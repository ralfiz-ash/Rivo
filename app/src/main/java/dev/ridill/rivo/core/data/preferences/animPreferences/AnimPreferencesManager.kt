package dev.ridill.rivo.core.data.preferences.animPreferences

import dev.ridill.rivo.core.domain.model.AnimPreferences
import kotlinx.coroutines.flow.Flow

interface AnimPreferencesManager {
    companion object {
        const val NAME = "Rivo_anim_preferences"
    }

    val preferences: Flow<AnimPreferences>

    suspend fun disableDashboardRecentSpendMarqueeTooltip()
    suspend fun disableScheduleItemActionPreview()
    suspend fun disableTxInFolderItemActionPreview()
}