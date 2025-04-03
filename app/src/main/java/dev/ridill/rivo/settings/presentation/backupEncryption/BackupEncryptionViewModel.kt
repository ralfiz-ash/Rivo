package dev.ridill.rivo.settings.presentation.backupEncryption

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupEncryptionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val repo: BackupSettingsRepository,
    private val eventBus: EventBus<BackupEncryptionEvent>
) : ViewModel(), BackupEncryptionActions {

    val currentPassword = savedStateHandle.getStateFlow(CURRENT_PASSWORD, "")
    val newPassword = savedStateHandle.getStateFlow(NEW_PASSWORD, "")
    val confirmNewPassword = savedStateHandle.getStateFlow(CONFIRM_NEW_PASSWORD, "")

    private val showPasswordInput = savedStateHandle.getStateFlow(SHOW_PASSWORD_INPUT, false)

    private val hasExistingPassword = securityPreferencesManager.preferences
        .mapLatest { it.hasValidBackupEncryptionPassword }
        .distinctUntilChanged()

    private val isLoading = MutableStateFlow(false)

    val state = combineTuple(
        showPasswordInput,
        hasExistingPassword,
        isLoading
    ).mapLatest { (
                      showPasswordInput,
                      hasExistingPassword,
                      isLoading
                  ) ->
        BackupEncryptionState(
            hasExistingPassword = hasExistingPassword,
            showPasswordInput = showPasswordInput,
            isLoading = isLoading
        )
    }.asStateFlow(viewModelScope, BackupEncryptionState())

    val events = eventBus.eventFlow

    override fun onUpdatePasswordClick() {
        savedStateHandle[SHOW_PASSWORD_INPUT] = true
    }

    override fun onCurrentPasswordChange(value: String) {
        savedStateHandle[CURRENT_PASSWORD] = value
    }

    override fun onForgotCurrentPasswordClick() {
        viewModelScope.launch {
            eventBus.send(BackupEncryptionEvent.LaunchBiometricAuthentication)
        }
    }

    fun onBiometricAuthSucceeded() = viewModelScope.launch {
        securityPreferencesManager.updateBackupEncryptionHash(null, null)
    }

    override fun onNewPasswordChange(value: String) {
        savedStateHandle[NEW_PASSWORD] = value
    }

    override fun onConfirmNewPasswordChange(value: String) {
        savedStateHandle[CONFIRM_NEW_PASSWORD] = value
    }

    override fun onPasswordInputDismiss() {
        savedStateHandle[SHOW_PASSWORD_INPUT] = false
        clearPasswordInputs()
    }

    override fun onPasswordUpdateConfirm() {
        viewModelScope.launch {
            isLoading.update { true }
            val currentPassword = currentPassword.value
            val newPassword = newPassword.value
            val confirmNewPassword = confirmNewPassword.value

            if (hasExistingPassword.first()) {
                if (!repo.isCurrentPasswordMatch(currentPassword)) {
                    savedStateHandle[SHOW_PASSWORD_INPUT] = false
                    clearPasswordInputs()
                    eventBus.send(
                        BackupEncryptionEvent.ShowUiMessage(
                            UiText.StringResource(
                                R.string.error_incorrect_existing_encryption_password,
                                true
                            )
                        )
                    )
                    isLoading.update { false }
                    return@launch
                }
            }

            if (newPassword != confirmNewPassword) {
                isLoading.update { false }
                savedStateHandle[SHOW_PASSWORD_INPUT] = false
                clearPasswordInputs()
                eventBus.send(
                    BackupEncryptionEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_passwords_do_not_match,
                            true
                        )
                    )
                )
                return@launch
            }

            repo.updateEncryptionPassword(newPassword)
            isLoading.update { false }
            savedStateHandle[SHOW_PASSWORD_INPUT] = false
            eventBus.send(BackupEncryptionEvent.PasswordUpdated)
            clearPasswordInputs()
        }
    }

    private fun clearPasswordInputs() {
        savedStateHandle[CURRENT_PASSWORD] = String.Empty
        savedStateHandle[NEW_PASSWORD] = String.Empty
        savedStateHandle[CONFIRM_NEW_PASSWORD] = String.Empty
    }

    sealed interface BackupEncryptionEvent {
        data class ShowUiMessage(val message: UiText) : BackupEncryptionEvent
        data object PasswordUpdated : BackupEncryptionEvent
        data object LaunchBiometricAuthentication : BackupEncryptionEvent
    }
}

const val ACTION_ENCRYPTION_PASSWORD = "ACTION_ENCRYPTION_PASSWORD"
const val ENCRYPTION_PASSWORD_UPDATED = "ENCRYPTION_PASSWORD_UPDATED"

private const val SHOW_PASSWORD_INPUT = "SHOW_PASSWORD_INPUT"
private const val CURRENT_PASSWORD = "CURRENT_PASSWORD"
private const val NEW_PASSWORD = "NEW_PASSWORD"
private const val CONFIRM_NEW_PASSWORD = "CONFIRM_NEW_PASSWORD"