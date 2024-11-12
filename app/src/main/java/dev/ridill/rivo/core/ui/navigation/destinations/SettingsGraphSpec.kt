package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

object SettingsGraphSpec : NavGraphSpec, BottomNavDestination {

    override val route: String
        get() = "settings_graph"

    override val labelRes: Int
        get() = R.string.destination_settings

    override val iconRes: Int
        get() = R.drawable.ic_outline_settings

    override val precedence: Int
        get() = 0

    override val children: List<NavDestination>
        get() = listOf(
            SettingsScreenSpec,
            UpdateBudgetSheetSpec,
            UpdateCurrencyPreferenceSheetSpec,
            BackupSettingsScreenSpec,
            BackupEncryptionScreenSpec,
            SecuritySettingsScreenSpec,
            AccountDetailsScreenSpec
        )
}