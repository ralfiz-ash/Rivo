package dev.ridill.rivo.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.appInit.AppInitWorkManager
import dev.ridill.rivo.settings.domain.appLock.AppLockServiceManager
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RivoViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiverService: ReceiverService,
    private val appLockServiceManager: AppLockServiceManager,
    private val backupWorkManager: BackupWorkManager,
    private val appInitWorkManager: AppInitWorkManager,
    currencyPreferenceRepo: CurrencyRepository,
    private val eventBus: EventBus<RivoEvent>
) : ViewModel() {
    private val preferences = preferencesManager.preferences

    val showOnboarding = preferences
        .map { it.showOnboarding }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, false)

    val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()

    val dynamicThemeEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    val isAppLocked = preferences
        .map { it.isAppLocked }
        .distinctUntilChanged()
        .onStart { startAppUnlockOrServiceStop() }
        .asStateFlow(viewModelScope, false)

    val appLockAuthErrorMessage = MutableStateFlow<UiText?>(null)

    val screenSecurityEnabled = preferences.map { it.screenSecurityEnabled }
        .distinctUntilChanged()

    val currencyPreference = currencyPreferenceRepo
        .getCurrencyPreferenceForMonth()
        .distinctUntilChanged()

    val events = eventBus.eventFlow

    init {
        collectTransactionAutoDetectEnabled()
        collectIsAppLocked()
        runInitIfNeeded()
    }

    private fun collectTransactionAutoDetectEnabled() = viewModelScope.launch {
        preferences.map { it.transactionAutoDetectEnabled }
            .distinctUntilChanged()
            .collectLatest { enabled ->
                receiverService.toggleSmsReceiver(enabled)
            }
    }

    fun onSmsPermissionCheck(granted: Boolean) = viewModelScope.launch {
        if (!granted) {
            preferencesManager.updateTransactionAutoDetectEnabled(false)
        }
    }

    fun onNotificationPermissionCheck(granted: Boolean) {
        receiverService.toggleNotificationActionReceivers(granted)
    }

    private fun collectIsAppLocked() = viewModelScope.launch {
        preferences
            .map { Pair(it.appLockEnabled, it.isAppLocked) }
            .distinctUntilChanged()
            .collectLatest { (appLockedEnabled, isLocked) ->
                if (!appLockedEnabled || isLocked) {
                    appLockServiceManager.stopAppUnlockedIndicator()
                } else {
                    appLockServiceManager.startAppUnlockedIndicator()
                }
            }
    }

    fun startAppAutoLockTimer() = viewModelScope.launch {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled || preferences.isAppLocked) return@launch

        appLockServiceManager.startAppAutoLockTimer()
    }

    private suspend fun startAppUnlockOrServiceStop() {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled) return
        if (preferences.isAppLocked) {
            eventBus.send(RivoEvent.LaunchBiometricAuthentication)
        } else {
            appLockServiceManager.stopAppLockTimer()
        }
    }

    fun onAppLockAuthSucceeded() = viewModelScope.launch {
        preferencesManager.updateAppLocked(false)
    }

    fun updateAppLockErrorMessage(message: UiText?) {
        appLockAuthErrorMessage.update { message }
    }

    fun startConfigRestore() = backupWorkManager.runConfigRestoreWork()

    private fun runInitIfNeeded() = viewModelScope.launch {
        appInitWorkManager.startAppInitWorker()
    }

    sealed class RivoEvent {
        data object LaunchBiometricAuthentication : RivoEvent()
    }
}