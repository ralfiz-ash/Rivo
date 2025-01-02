package dev.ridill.rivo.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: SettingsRepository,
    private val eventBus: EventBus<SettingsEvent>
) : ViewModel(), SettingsActions {

    private val authState = repo.getAuthState()

    private val appTheme = repo.getCurrentAppTheme()
    private val dynamicColorsEnabled = repo.getDynamicColorsEnabled()

    private val currentBudget = repo.getCurrentBudget()

    private val transactionAutoDetectEnabled = repo.getTransactionAutoDetectEnabled()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)

    private val showSmsPermissionRationale = savedStateHandle
        .getStateFlow(SHOW_SMS_PERMISSION_RATIONALE, false)

    private val showTransactionAutoDetectInfo = savedStateHandle
        .getStateFlow(SHOW_AUTO_DETECT_TX_INFO, false)

    val state = combineTuple(
        authState,
        appTheme,
        dynamicColorsEnabled,
        showAppThemeSelection,
        currentBudget,
        transactionAutoDetectEnabled,
        showSmsPermissionRationale,
        showTransactionAutoDetectInfo
    ).map { (
                authState,
                appTheme,
                dynamicColorsEnabled,
                showAppThemeSelection,
                monthlyBudget,
                autoAddTransactionEnabled,
                showSmsPermissionRationale,
                showTransactionAutoDetectInfo
            ) ->
        SettingsState(
            authState = authState,
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicColorsEnabled,
            showAppThemeSelection = showAppThemeSelection,
            currentMonthlyBudget = monthlyBudget,
            autoAddTransactionEnabled = autoAddTransactionEnabled,
            showSmsPermissionRationale = showSmsPermissionRationale,
            showAutoDetectTransactionFeatureInfo = showTransactionAutoDetectInfo
        )
    }
        .onStart { repo.refreshCurrentDate() }
        .asStateFlow(viewModelScope, SettingsState())

    val events = eventBus.eventFlow

    override fun onAppThemePreferenceClick() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = true
    }

    override fun onAppThemeSelectionDismiss() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = false
    }

    override fun onAppThemeSelectionConfirm(appTheme: AppTheme) {
        viewModelScope.launch {
            savedStateHandle[SHOW_APP_THEME_SELECTION] = false
            repo.updateAppTheme(appTheme)
        }
    }

    override fun onDynamicThemeEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            repo.toggleDynamicColors(enabled)
        }
    }

    override fun onToggleAutoAddTransactions(enabled: Boolean) {
        viewModelScope.launch {
            savedStateHandle[TEMP_AUTO_ADD_TRANSACTION_STATE] = enabled
            if (repo.getShowTransactionAutoDetectInfoValue() && enabled) {
                savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = true
            } else {
                eventBus.send(SettingsEvent.RequestSMSPermission)
            }
        }
    }

    override fun onAutoDetectTxFeatureInfoDismiss() {
        savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = false
    }

    override fun onAutoDetectTxFeatureInfoAcknowledge() {
        viewModelScope.launch {
            repo.toggleShowAutoDetectTxInfoFalse()
            savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = false
            eventBus.send(SettingsEvent.RequestSMSPermission)
        }
    }

    fun onSmsPermissionResult(granted: Boolean) = viewModelScope.launch {
        if (granted) {
            val enabled = savedStateHandle.get<Boolean?>(TEMP_AUTO_ADD_TRANSACTION_STATE) == true
            repo.toggleAutoDetectTransactions(enabled)
            savedStateHandle[TEMP_AUTO_ADD_TRANSACTION_STATE] = null
        }
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = !granted
    }

    override fun onSmsPermissionRationaleDismiss() {
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
    }

    override fun onSmsPermissionRationaleSettingsClick() {
        viewModelScope.launch {
            savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
            eventBus.send(SettingsEvent.LaunchAppSettings)
        }
    }

    fun onBaseCurrencySelected(currency: Currency) = viewModelScope.launch {
        repo.updateCurrencyPreference(currency)
        eventBus.send(SettingsEvent.ShowUiMessage(UiText.StringResource(R.string.currency_updated)))
    }

    sealed interface SettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : SettingsEvent
        data object RequestSMSPermission : SettingsEvent
        data object LaunchAppSettings : SettingsEvent
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"
private const val SHOW_SMS_PERMISSION_RATIONALE = "SHOW_SMS_PERMISSION_RATIONALE"
private const val TEMP_AUTO_ADD_TRANSACTION_STATE = "TEMP_AUTO_ADD_TRANSACTION_STATE"
private const val SHOW_AUTO_DETECT_TX_INFO = "SHOW_AUTO_DETECT_TX_INFO"