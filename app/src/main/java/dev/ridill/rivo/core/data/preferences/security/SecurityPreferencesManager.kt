package dev.ridill.rivo.core.data.preferences.security

import dev.ridill.rivo.core.domain.model.SecurityPreferences
import kotlinx.coroutines.flow.Flow

interface SecurityPreferencesManager {
    companion object {
        const val NAME = "Rivo_security_preferences"
    }

    val preferences: Flow<SecurityPreferences>

    suspend fun updateBackupEncryptionHash(hash: String?, salt: String?)
}