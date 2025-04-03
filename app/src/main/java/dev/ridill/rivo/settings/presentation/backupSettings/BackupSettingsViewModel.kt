package dev.ridill.rivo.settings.presentation.backupSettings

import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.account.presentation.util.AuthorizationService
import dev.ridill.rivo.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.rivo.settings.presentation.backupEncryption.ENCRYPTION_PASSWORD_UPDATED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val backupSettingsRepo: BackupSettingsRepository,
    private val authRepo: AuthRepository,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val eventBus: EventBus<BackupSettingsEvent>
) : ViewModel(), BackupSettingsActions {

    private val authState = authRepo.getAuthState()
        .distinctUntilChanged()

    private val backupInterval = MutableStateFlow(BackupInterval.MANUAL)

    private val showBackupIntervalSelection = savedStateHandle
        .getStateFlow(SHOW_BACKUP_INTERVAL_SELECTION, false)

    private val lastBackupDateTime = backupSettingsRepo.getLastBackupTime()
        .distinctUntilChanged()

    private val isBackupRunning = MutableStateFlow(false)

    private val isEncryptionPasswordAvailable = backupSettingsRepo.isEncryptionPasswordAvailable()

    private val fatalBackupError = backupSettingsRepo.getFatalBackupError()

    private val showBackupRunningMessage = savedStateHandle
        .getStateFlow(SHOW_BACKUP_RUNNING_MESSAGE, false)

    val state = combineTuple(
        authState,
        backupInterval,
        showBackupIntervalSelection,
        lastBackupDateTime,
        isBackupRunning,
        isEncryptionPasswordAvailable,
        fatalBackupError,
        showBackupRunningMessage
    ).mapLatest { (
                      authState,
                      backupInterval,
                      showBackupIntervalSelection,
                      lastBackupDateTime,
                      isBackupRunning,
                      isEncryptionPasswordAvailable,
                      fatalBackupError,
                      showBackupRunningMessage

                  ) ->
        BackupSettingsState(
            authState = authState,
            backupInterval = backupInterval,
            showBackupIntervalSelection = showBackupIntervalSelection,
            lastBackupDateTime = lastBackupDateTime,
            isBackupRunning = isBackupRunning,
            isEncryptionPasswordAvailable = isEncryptionPasswordAvailable,
            fatalBackupError = fatalBackupError,
            showBackupRunningMessage = showBackupRunningMessage
        )
    }.asStateFlow(viewModelScope, BackupSettingsState())

    val events = eventBus.eventFlow

    init {
        collectImmediateBackupWorkInfo()
        collectPeriodicBackupWorkInfo()
    }

    private var hasBackupJobRunThisSession: Boolean = false
    private fun collectImmediateBackupWorkInfo() = viewModelScope.launch {
        backupSettingsRepo.getImmediateBackupWorkInfo().collectLatest { info ->
            logD { "Immediate Backup Work Info - $info" }
            val isRunning = info?.state == WorkInfo.State.RUNNING
            isBackupRunning.update { isRunning }
            if (isRunning) {
                hasBackupJobRunThisSession = true
            }

            // if check to prevent showing message without running backup job at least once.
            // hasBackupJobRunThisSession boolean is set to true when backup job is running.
            // Without this check WorkInfo output message will be shown everytime user arrives at screen
            // Even if backup was run long back
            if (hasBackupJobRunThisSession) {
                info?.outputData?.getString(BackupWorkManager.KEY_MESSAGE)?.let {
                    eventBus.send(BackupSettingsEvent.ShowUiMessage(UiText.DynamicString(it)))
                }
            }
        }
    }

    private fun collectPeriodicBackupWorkInfo() = viewModelScope.launch {
        backupSettingsRepo.getPeriodicBackupWorkInfo().collectLatest { info ->
            updateBackupInterval(info)
            logD { "Periodic Backup Work Info - $info" }
            isBackupRunning.update {
                info?.state == WorkInfo.State.RUNNING
            }
        }
    }

    private fun updateBackupInterval(info: WorkInfo?) = viewModelScope.launch {
        val interval = if (info?.state == WorkInfo.State.CANCELLED)
            BackupInterval.MANUAL
        else info?.let(backupSettingsRepo::getIntervalFromInfo)
        backupInterval.update { interval ?: BackupInterval.MANUAL }
    }

    override fun onBackupAccountClick() {
        if (isBackupRunning.value) {
            savedStateHandle[SHOW_BACKUP_RUNNING_MESSAGE] = true
            return
        }
        viewModelScope.launch {
            eventBus.send(BackupSettingsEvent.NavigateToAccountDetailsPage)
        }
    }

    override fun onBackupIntervalPreferenceClick() {
        viewModelScope.launch {
            if (securityPreferencesManager.preferences.first().hasValidBackupEncryptionPassword) {
                eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
                return@launch
            }
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = true
        }
    }

    override fun onBackupIntervalSelected(interval: BackupInterval) {
        viewModelScope.launch {
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
            backupSettingsRepo.updateBackupIntervalAndScheduleJob(interval)
        }
    }

    override fun onBackupIntervalSelectionDismiss() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
    }

    override fun onBackupNowClick() {
        viewModelScope.launch {
            if (!securityPreferencesManager.preferences.first().hasValidBackupEncryptionPassword) {
                eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
                return@launch
            }
            when (val result = authRepo.authorizeUserAccount()) {
                is Result.Error -> {
                    when (result.error) {
                        AuthorizationService.AuthorizationError.NEEDS_RESOLUTION -> {
                            result.data?.let {
                                eventBus.send(BackupSettingsEvent.StartAuthorizationFlow(it))
                            }
                        }

                        AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED -> {
                            eventBus.send(BackupSettingsEvent.ShowUiMessage(result.message))
                        }
                    }
                }

                is Result.Success -> {
                    backupSettingsRepo.runImmediateBackupJob()
                }
            }
        }
    }

    fun onAuthorizationResult(intent: Intent) = viewModelScope.launch {
        when (val result = authRepo.decodeAuthorizationResult(intent)) {
            is Result.Error -> {
                eventBus.send(BackupSettingsEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> Unit
        }
    }

    override fun onEncryptionPreferenceClick() {
        viewModelScope.launch {
            eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
        }
    }

    fun onDestinationResult(result: String) {
        viewModelScope.launch {
            when (result) {
                ENCRYPTION_PASSWORD_UPDATED -> {
                    eventBus.send(BackupSettingsEvent.ShowUiMessage(UiText.StringResource(R.string.encryption_password_updated)))
                    val interval = backupInterval.first()
                    backupSettingsRepo.runBackupJob(interval)
                }

                else -> Unit
            }
        }
    }

    override fun onBackupRunningMessageAcknowledge() {
        savedStateHandle[SHOW_BACKUP_RUNNING_MESSAGE] = false
    }

    sealed interface BackupSettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : BackupSettingsEvent
        data object NavigateToBackupEncryptionScreen : BackupSettingsEvent
        data object NavigateToAccountDetailsPage : BackupSettingsEvent
        data class StartAuthorizationFlow(val pendingIntent: PendingIntent) : BackupSettingsEvent
    }
}

private const val SHOW_BACKUP_INTERVAL_SELECTION = "SHOW_BACKUP_INTERVAL_SELECTION"
private const val SHOW_BACKUP_RUNNING_MESSAGE = "SHOW_BACKUP_RUNNING_MESSAGE"