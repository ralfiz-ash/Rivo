package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    fun refreshCurrentDate()
    fun getAuthState(): Flow<AuthState>
    fun getCurrentAppTheme(): Flow<AppTheme>
    suspend fun updateAppTheme(theme: AppTheme)
    fun getDynamicColorsEnabled(): Flow<Boolean>
    suspend fun toggleDynamicColors(enabled: Boolean)
    fun getCurrentBudget(): Flow<Long>
    suspend fun updateCurrencyPreference(currency: Currency)
    fun getTransactionAutoDetectEnabled(): Flow<Boolean>
    suspend fun toggleAutoDetectTransactions(enabled: Boolean)
    suspend fun getShowTransactionAutoDetectInfoValue(): Boolean
    suspend fun toggleShowAutoDetectTxInfoFalse()
}