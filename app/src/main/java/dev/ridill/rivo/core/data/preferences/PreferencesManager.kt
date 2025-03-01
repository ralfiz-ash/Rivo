package dev.ridill.rivo.core.data.preferences

import dev.ridill.rivo.core.domain.model.RivoPreferences
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PreferencesManager {
    companion object {
        const val NAME = "Rivo_preferences"
    }

    val preferences: Flow<RivoPreferences>

    suspend fun concludeOnboarding()
    suspend fun updateAppThem(theme: AppTheme)
    suspend fun updateDynamicColorsEnabled(enabled: Boolean)
    suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime)
    suspend fun updateTransactionAutoDetectEnabled(enabled: Boolean)
    suspend fun updateAllTransactionsShowExcludedOption(show: Boolean)
    suspend fun updateAppLockEnabled(enabled: Boolean)
    suspend fun updateAppAutoLockInterval(interval: AppAutoLockInterval)
    suspend fun updateAppLocked(locked: Boolean)
    suspend fun updateScreenSecurityEnabled(enabled: Boolean)
    suspend fun updateEncryptionPasswordHash(hash: String?)
    suspend fun updateFatalBackupError(error: FatalBackupError?)
    suspend fun toggleShowAutoDetectTxInfoFalse()
}