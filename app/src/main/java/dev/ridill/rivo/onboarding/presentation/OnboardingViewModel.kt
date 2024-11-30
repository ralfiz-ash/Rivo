package dev.ridill.rivo.onboarding.presentation

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.account.presentation.util.AuthorizationService
import dev.ridill.rivo.account.presentation.util.CredentialService
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.onboarding.domain.model.DataRestoreState
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<OnboardingEvent>,
    private val backupWorkManager: BackupWorkManager,
    private val budgetRepo: BudgetPreferenceRepository,
    private val preferencesManager: PreferencesManager,
    private val backupRepo: BackupRepository,
    private val authRepo: AuthRepository,
    private val cryptoManager: CryptoManager
) : ViewModel(), OnboardingActions {

    val authState = authRepo.getAuthState()
    private val _dataRestoreState = MutableStateFlow(DataRestoreState.IDLE)
    val dataRestoreState get() = _dataRestoreState.asStateFlow()
    val showEncryptionPasswordInput = savedStateHandle
        .getStateFlow(SHOW_ENCRYPTION_PASSWORD_INPUT, false)

    val budgetInput = savedStateHandle.getStateFlow(BUDGET_INPUT, "")

    val events = eventBus.eventFlow

    init {
        collectRestoreWorkState()
    }

    private var hasRestoreJobRunThisSession: Boolean = false
    private fun collectRestoreWorkState() = viewModelScope.launch {
        combineTuple(
            backupWorkManager.getRestoreDataDownloadWorkInfoFlow(),
            backupWorkManager.getImmediateDataRestoreWorkInfoFlow()
        ).collectLatest { (downloadInfo, restoreInfo) ->
            val isDownloadRunning = downloadInfo?.state == WorkInfo.State.RUNNING
            val isRestoreRunning = restoreInfo?.state == WorkInfo.State.RUNNING
            _dataRestoreState.update {
                when {
                    isDownloadRunning -> DataRestoreState.DOWNLOADING_DATA
                    isRestoreRunning -> DataRestoreState.RESTORE_IN_PROGRESS
                    else -> it
                }
            }

            if (isDownloadRunning || isRestoreRunning) {
                hasRestoreJobRunThisSession = true
            }

            when {
                restoreInfo?.state == WorkInfo.State.SUCCEEDED -> {
                    _dataRestoreState.update { DataRestoreState.COMPLETED }
                    savedStateHandle[AVAILABLE_BACKUP] = null
                    preferencesManager.concludeOnboarding()
                    delay(5.seconds)
                    eventBus.send(OnboardingEvent.RestartApplication)
                }

                downloadInfo?.state == WorkInfo.State.FAILED -> {
                    _dataRestoreState.update { DataRestoreState.FAILED }
                    if (hasRestoreJobRunThisSession) eventBus.send(
                        OnboardingEvent.ShowUiMessage(
                            downloadInfo.outputData.getString(BackupWorkManager.KEY_MESSAGE)
                                ?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(
                                    R.string.error_app_data_restore_failed,
                                    true
                                )
                        )
                    )
                }

                restoreInfo?.state == WorkInfo.State.FAILED -> {
                    _dataRestoreState.update { DataRestoreState.FAILED }
                    if (hasRestoreJobRunThisSession) eventBus.send(
                        OnboardingEvent.ShowUiMessage(
                            restoreInfo.outputData.getString(BackupWorkManager.KEY_MESSAGE)
                                ?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(
                                    R.string.error_app_data_restore_failed,
                                    true
                                )
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    override fun onGivePermissionsClick() {
        viewModelScope.launch {
            if (BuildUtil.isNotificationRuntimePermissionNeeded())
                eventBus.send(OnboardingEvent.LaunchNotificationPermissionRequest)
            else
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.ACCOUNT_SIGN_IN))
        }
    }

    override fun onSkipPermissionsClick() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.ACCOUNT_SIGN_IN))
        }
    }

    fun onPermissionsRequestResult(result: Map<String, Boolean>) {
        viewModelScope.launch {
            val isSMSPermissionGranted = result[Manifest.permission.RECEIVE_SMS] == true
            preferencesManager.updateTransactionAutoDetectEnabled(isSMSPermissionGranted)

            val areAllGranted = result.all { it.value }
            if (areAllGranted)
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.ACCOUNT_SIGN_IN))
        }
    }

    private var pageChangeJob: Job? = null
    fun onPageChange(page: Int) {
        pageChangeJob?.cancel()
        pageChangeJob = viewModelScope.launch {
            when (page) {
                OnboardingPage.ACCOUNT_SIGN_IN.ordinal -> {
                    onAccountPageReached()
                }
            }
        }
    }

    private suspend fun onAccountPageReached() {
        val isUserUnAuthenticated = authRepo.getAuthState().first() == AuthState.UnAuthenticated
        if (isUserUnAuthenticated) {
            delay(400L)
            eventBus.send(OnboardingEvent.StartAutoSignInFlow(true))
        }
    }

    fun onCredentialResult(
        result: Result<String, CredentialService.CredentialError>
    ) = viewModelScope.launch {
        when (result) {
            is Result.Error -> {
                when (result.error) {
                    CredentialService.CredentialError.NO_AUTHORIZED_CREDENTIAL -> {
                        eventBus.send(OnboardingEvent.StartAutoSignInFlow(false))
                    }

                    CredentialService.CredentialError.CREDENTIAL_PROCESS_FAILED -> eventBus.send(
                        OnboardingEvent.ShowUiMessage(result.message)
                    )
                }
            }

            is Result.Success -> {
                signInUser(result.data)
            }
        }
    }

    private suspend fun signInUser(idToken: String) {
        when (val result = authRepo.signUserInWithToken(idToken)) {
            is Result.Error -> {
                eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.DATA_RESTORE))
            }
        }
    }

    private var signInJob: Job? = null
    override fun onSignInClick() {
        signInJob?.cancel()
        signInJob = viewModelScope.launch {
            eventBus.send(OnboardingEvent.StartManualSignInFlow)
        }
    }

    override fun onSkipSignInClick() {
        viewModelScope.launch {
            val isAuthenticated = authState.first() is AuthState.Authenticated
            eventBus.send(
                OnboardingEvent.NavigateToPage(
                    if (isAuthenticated) OnboardingPage.DATA_RESTORE
                    else OnboardingPage.SET_BUDGET
                )
            )
        }
    }

    private var backupJob: Job? = null
    override fun onCheckOrRestoreClick() {
        backupJob?.cancel()
        backupJob = viewModelScope.launch {
            when (val result = authRepo.authorizeUserAccount()) {
                is Result.Error -> {
                    when (result.error) {
                        AuthorizationService.AuthorizationError.NEEDS_RESOLUTION -> {
                            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.ACCOUNT_SIGN_IN))
                        }

                        AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED -> {
                            eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
                        }
                    }
                }

                is Result.Success -> {
                    checkIfBackupExists()
                }
            }
        }
    }

    fun onAuthorizationResult(intent: Intent) = viewModelScope.launch {
        when (val result = authRepo.decodeAuthorizationResult(intent)) {
            is Result.Error -> {
                eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                checkIfBackupExists()
            }
        }
    }

    private suspend fun checkIfBackupExists() {
        if (savedStateHandle.get<BackupDetails?>(AVAILABLE_BACKUP) != null) {
            _dataRestoreState.update { DataRestoreState.PASSWORD_VERIFICATION }
            savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = true
            return
        }
        _dataRestoreState.update { DataRestoreState.CHECKING_FOR_BACKUP }
        logI { "Running Backup Check" }
        when (val result = backupRepo.checkForBackup()) {
            is Result.Error -> {
                _dataRestoreState.update { DataRestoreState.IDLE }
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
            }

            is Result.Success -> {
                _dataRestoreState.update { DataRestoreState.PASSWORD_VERIFICATION }
                savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = true
                savedStateHandle[AVAILABLE_BACKUP] = result.data
            }
        }
    }

    override fun onEncryptionPasswordInputDismiss() {
        savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = false
    }

    override fun onEncryptionPasswordSubmit(password: String) {
        val backupDetails = savedStateHandle.get<BackupDetails?>(AVAILABLE_BACKUP)
            ?: return
        val passwordHash = cryptoManager.hash(password)
        savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = false
        backupWorkManager.runImmediateRestoreWork(
            backupDetails = backupDetails,
            passwordHash = passwordHash
        )
    }

    override fun onDataRestoreSkip() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
            savedStateHandle[AVAILABLE_BACKUP] = null
        }
    }

    override fun onBudgetInputChange(value: String) {
        savedStateHandle[BUDGET_INPUT] = value
    }

    override fun onStartBudgetingClick() {
        viewModelScope.launch {
            val budgetValue = budgetInput.value.toLongOrNull() ?: -1L
            if (budgetValue <= Long.Zero) {
                eventBus.send(
                    OnboardingEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }
            budgetRepo.saveBudgetPreference(budgetValue)
            preferencesManager.concludeOnboarding()
            eventBus.send(OnboardingEvent.OnboardingConcluded)
        }
    }

    sealed interface OnboardingEvent {
        data class NavigateToPage(val page: OnboardingPage) : OnboardingEvent
        data object OnboardingConcluded : OnboardingEvent
        data class ShowUiMessage(val uiText: UiText) : OnboardingEvent
        data object LaunchNotificationPermissionRequest : OnboardingEvent
        data class StartAutoSignInFlow(val filterByAuthorizedAccounts: Boolean) :
            OnboardingEvent

        data object StartManualSignInFlow : OnboardingEvent
        data class StartAuthorizationFlow(val pendingIntent: PendingIntent) : OnboardingEvent
        data object RestartApplication : OnboardingEvent
    }
}

private const val BUDGET_INPUT = "BUDGET_INPUT"

private const val AVAILABLE_BACKUP = "AVAILABLE_BACKUP"
private const val SHOW_ENCRYPTION_PASSWORD_INPUT = "SHOW_ENCRYPTION_PASSWORD_INPUT"