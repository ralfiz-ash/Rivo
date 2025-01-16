package dev.ridill.rivo.settings.presentation.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.ridill.rivo.BuildConfig
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.FeatureInfoDialog
import dev.ridill.rivo.core.ui.components.PermissionRationaleDialog
import dev.ridill.rivo.core.ui.components.RadioOptionListDialog
import dev.ridill.rivo.core.ui.components.RivoImage
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.TitleMediumText
import dev.ridill.rivo.core.ui.components.icons.Message
import dev.ridill.rivo.core.ui.navigation.destinations.SettingsScreenSpec
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.presentation.components.PreferenceIcon
import dev.ridill.rivo.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.rivo.settings.presentation.components.SwitchPreference
import java.util.Currency

@Composable
fun SettingsScreen(
    snackbarController: SnackbarController,
    currencyPreference: Currency,
    state: SettingsState,
    actions: SettingsActions,
    navigateUp: () -> Unit,
    navigateToAccountDetails: () -> Unit,
    navigateToNotificationSettings: () -> Unit,
    navigateToUpdateBudget: () -> Unit,
    navigateToCurrencySelection: () -> Unit,
    navigateToManageTags: () -> Unit,
    navigateToBackupSettings: () -> Unit,
    navigateToSecuritySettings: () -> Unit,
    launchUriInBrowser: (Uri) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SettingsScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = PaddingScrollEnd)
        ) {
            AccountInfo(
                onClick = navigateToAccountDetails,
                authState = state.authState
            )
            SimpleSettingsPreference(
                titleRes = R.string.preference_app_theme,
                summary = stringResource(state.appTheme.labelRes),
                onClick = actions::onAppThemePreferenceClick,
                leadingIcon = Icons.Rounded.BrightnessMedium
            )

            if (BuildUtil.isDynamicColorsSupported()) {
                SwitchPreference(
                    titleRes = R.string.preference_dynamic_colors,
                    summary = stringResource(R.string.preference_dynamic_colors_summary),
                    value = state.dynamicColorsEnabled,
                    onValueChange = actions::onDynamicThemeEnabledChange,
                    leadingIcon = { PreferenceIcon(imageVector = Icons.Rounded.Palette) }
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_notifications,
                summary = stringResource(R.string.preference_notification_summary),
                onClick = navigateToNotificationSettings,
                leadingIcon = Icons.Rounded.Notifications
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = PreferenceDividerVerticalPadding)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_budget,
                summary = state.currentMonthlyBudget.takeIf { it > Long.Zero }
                    ?.let {
                        stringResource(
                            R.string.preference_current_budget_summary,
                            TextFormat.currencyAmount(it)
                        )
                    }
                    ?: stringResource(R.string.preference_set_budget_summary),
                onClick = navigateToUpdateBudget
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_base_currency,
                summary = stringResource(
                    R.string.preference_base_currency_summary,
                    currencyPreference.displayName,
                    currencyPreference.currencyCode
                ),
                onClick = navigateToCurrencySelection
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_tags,
                summary = stringResource(R.string.preference_tags_summary),
                onClick = navigateToManageTags
            )

            SwitchPreference(
                titleRes = R.string.preference_auto_detect_transactions,
                summary = stringResource(R.string.preference_auto_detect_transactions_summary),
                value = state.autoAddTransactionEnabled,
                onValueChange = actions::onToggleAutoAddTransactions
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_backup,
                summary = stringResource(R.string.preference_backup_summary),
                onClick = navigateToBackupSettings,
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_security,
                summary = stringResource(R.string.preference_security_summary),
                onClick = navigateToSecuritySettings
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = PreferenceDividerVerticalPadding)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_source_code,
                summary = stringResource(R.string.preference_source_code_summary),
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_filled_source_code),
                trailingIcon = Icons.AutoMirrored.Filled.Launch,
                onClick = {
                    launchUriInBrowser(
                        BuildConfig.SOURCE_CODE_URL.toUri()
                    )
                }
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_version,
                leadingIcon = Icons.Rounded.Info,
                summary = BuildUtil.versionName
            )
        }

        if (state.showAppThemeSelection) {
            RadioOptionListDialog(
                titleRes = R.string.choose_theme,
                options = AppTheme.entries,
                currentOption = state.appTheme,
                onDismiss = actions::onAppThemeSelectionDismiss,
                onOptionSelect = actions::onAppThemeSelectionConfirm
            )
        }

        if (state.showSmsPermissionRationale) {
            PermissionRationaleDialog(
                icon = Icons.Rounded.Message,
                rationaleText = stringResource(
                    R.string.permission_rationale_read_sms, stringResource(R.string.app_name)
                ),
                onDismiss = actions::onSmsPermissionRationaleDismiss,
                onSettingsClick = actions::onSmsPermissionRationaleSettingsClick
            )
        }

        if (state.showAutoDetectTransactionFeatureInfo) {
            FeatureInfoDialog(
                title = stringResource(R.string.feature_info_auto_detect_transaction_title),
                text = stringResource(
                    R.string.feature_info_auto_detect_transaction_message,
                    stringResource(R.string.app_name)
                ),
                onAcknowledge = actions::onAutoDetectTxFeatureInfoAcknowledge,
                onDismiss = actions::onAutoDetectTxFeatureInfoDismiss,
                isExperimental = true
            )
        }
    }
}

private val PreferenceDividerVerticalPadding = 12.dp

@Composable
private fun AccountInfo(
    onClick: () -> Unit,
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    val isAccountAuthenticate by remember(authState) {
        derivedStateOf { authState is AuthState.Authenticated }
    }
    val accountInfo = remember(authState) {
        tryOrNull(tag = "AccountInfo") { (authState as AuthState.Authenticated).account }
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = isAccountAuthenticate) {
                RivoImage(
                    url = accountInfo?.photoUrl.orEmpty(),
                    contentDescription = accountInfo?.displayName,
                    size = ProfileImageSize,
                    placeholderRes = R.drawable.ic_rounded_person,
                    errorRes = R.drawable.ic_rounded_person,
                )
            }
            SpacerMedium()
            Column {
                TitleMediumText(
                    text = when (authState) {
                        is AuthState.Authenticated -> authState.account.displayName
                        AuthState.UnAuthenticated -> stringResource(R.string.login_to_your_account)
                    }
                )
                AnimatedVisibility(visible = isAccountAuthenticate) {
                    BodyMediumText(
                        text = accountInfo?.displayName.orEmpty(),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                }
            }
        }
    }
}

private val ProfileImageSize = 40.dp

@Preview(showBackground = true)
@Composable
fun PreviewSimplePreference() {
    RivoTheme {
        Surface {
            SimpleSettingsPreference(
                titleRes = R.string.preference_app_theme,
                modifier = Modifier
                    .fillMaxWidth(),
                summary = stringResource(AppTheme.SYSTEM_DEFAULT.labelRes),
                leadingIcon = Icons.Rounded.BrightnessMedium
            )
        }
    }
}