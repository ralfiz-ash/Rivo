package dev.ridill.rivo.onboarding.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.isAnyOf
import dev.ridill.rivo.core.ui.components.GoogleSignInButton
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.OutlinedTextFieldSheet
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.TitleLargeText
import dev.ridill.rivo.core.ui.components.autofill
import dev.ridill.rivo.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.onboarding.domain.model.DataRestoreState
import dev.ridill.rivo.onboarding.domain.model.SignInAndDataRestoreState
import dev.ridill.rivo.settings.presentation.components.LoggedInAccountInfo
import kotlin.time.Duration

@Composable
fun SignInAndDataRestore(
    state: SignInAndDataRestoreState,
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSignInSkip: () -> Unit,
    restoreState: DataRestoreState,
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    showEncryptionPasswordInput: Boolean,
    onEncryptionPasswordInputDismiss: () -> Unit,
    onEncryptionPasswordSubmit: (String) -> Unit,
    appRestartTimer: Duration,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Crossfade(state) { screenState ->
            MediumDisplayText(
                text = stringResource(
                    id = when (screenState) {
                        SignInAndDataRestoreState.SIGN_IN -> R.string.onboarding_page_sign_in_title
                        SignInAndDataRestoreState.DATA_RESTORE -> R.string.onboarding_page_restore_data_title
                    }
                ),
                modifier = Modifier
                    .padding(vertical = MaterialTheme.spacing.medium)
            )
        }
        Crossfade(state) { screenState ->
            TitleLargeText(
                text = stringResource(
                    id = when (screenState) {
                        SignInAndDataRestoreState.SIGN_IN -> R.string.onboarding_page_sign_in_message
                        SignInAndDataRestoreState.DATA_RESTORE -> R.string.onboarding_page_restore_data_message
                    }
                ),
                fontWeight = FontWeight.Normal
            )
        }

        SpacerMedium()

        AnimatedVisibility(
            visible = state == SignInAndDataRestoreState.DATA_RESTORE,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_filled_cloud_download),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(DOWNLOAD_ICON_HEIGHT_FRACTION)
                    .aspectRatio(Float.One)
            )
        }

        Spacer(weight = Float.One)

        AnimatedContent(
            targetState = state,
            label = "SignInAndDataRestoreContent"
        ) { state ->
            when (state) {
                SignInAndDataRestoreState.SIGN_IN -> {
                    AccountSignInSection(
                        authState = authState,
                        onSignInClick = onSignInClick,
                        onSignInSkip = onSignInSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                SignInAndDataRestoreState.DATA_RESTORE -> {
                    DataRestoreSection(
                        restoreState = restoreState,
                        onCheckForBackupClick = onCheckForBackupClick,
                        onSkipClick = onSkipClick,
                        showEncryptionPasswordInput = showEncryptionPasswordInput,
                        onEncryptionPasswordInputDismiss = onEncryptionPasswordInputDismiss,
                        onEncryptionPasswordSubmit = onEncryptionPasswordSubmit,
                        appRestartTimer = appRestartTimer
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSignInSection(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSignInSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(authState) { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    LoggedInAccountInfo(
                        account = state.account,
                        modifier = Modifier
                            .clickable(
                                onClick = onSignInClick,
                                onClickLabel = stringResource(R.string.cd_sign_in)
                            )
                            .padding(MaterialTheme.spacing.small)
                    )
                }

                AuthState.UnAuthenticated -> {
                    GoogleSignInActions(
                        authState = authState,
                        onSignInClick = onSignInClick,
                        onSkipClick = onSignInSkip
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInActions(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showSignInOption by remember {
        derivedStateOf { authState is AuthState.UnAuthenticated }
    }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AnimatedVisibility(visible = showSignInOption) {
            GoogleSignInButton(
                onClick = onSignInClick
            )
        }

        TextButton(
            onClick = onSkipClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current
            )
        ) {
            Text(
                stringResource(
                    id = when (authState) {
                        is AuthState.Authenticated -> R.string.action_next
                        AuthState.UnAuthenticated -> R.string.action_skip
                    }
                )
            )
        }
    }
}

@Composable
private fun DataRestoreSection(
    restoreState: DataRestoreState,
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    showEncryptionPasswordInput: Boolean,
    onEncryptionPasswordInputDismiss: () -> Unit,
    onEncryptionPasswordSubmit: (String) -> Unit,
    appRestartTimer: Duration,
    modifier: Modifier = Modifier
) {
    val showRestoreStatus by remember(restoreState) {
        derivedStateOf {
            restoreState != DataRestoreState.IDLE
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = showRestoreStatus,
            enter = slideInVerticallyWithFadeIn(),
            exit = slideOutVerticallyWithFadeOut()
        ) {
            RestoreStatus(
                dataRestoreState = restoreState,
                restartTimer = appRestartTimer,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        val isBackupDownloaded by remember(restoreState) {
            derivedStateOf {
                restoreState.isAnyOf(
                    DataRestoreState.PASSWORD_VERIFICATION,
                    DataRestoreState.RESTORE_IN_PROGRESS
                )
            }
        }
        val isRestoreNotCompleted by remember(restoreState) {
            derivedStateOf {
                restoreState != DataRestoreState.COMPLETED
            }
        }
        val isRestoreInProgress by remember(restoreState) {
            derivedStateOf {
                restoreState.isAnyOf(
                    DataRestoreState.DOWNLOADING_DATA,
                    DataRestoreState.RESTORE_IN_PROGRESS
                )
            }
        }

        AnimatedVisibility(
            visible = isRestoreNotCompleted,
            enter = slideInVerticallyWithFadeIn(),
            exit = slideOutVerticallyWithFadeOut()
        ) {
            RestoreBackupActions(
                isBackupDownloaded = isBackupDownloaded,
                isRestoreInProgress = isRestoreInProgress,
                onRestoreClick = onCheckForBackupClick,
                onSkipClick = onSkipClick
            )
        }

        if (showEncryptionPasswordInput) {
            val passwordInput = remember { mutableStateOf("") }
            OutlinedTextFieldSheet(
                titleRes = R.string.enter_password,
                inputValue = { passwordInput.value },
                onValueChange = { passwordInput.value = it },
                onDismiss = onEncryptionPasswordInputDismiss,
                onConfirm = {
                    onEncryptionPasswordSubmit(passwordInput.value)
                    passwordInput.value = ""
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                textFieldModifier = Modifier.autofill(
                    types = listOf(AutofillType.Password),
                    onFill = { passwordInput.value = it }
                ),
                label = stringResource(R.string.enter_password)
            )
        }
    }
}

@Composable
private fun RestoreStatus(
    dataRestoreState: DataRestoreState,
    restartTimer: Duration,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = dataRestoreState,
            label = "RestoreWorkerStateText"
        ) { restoreState ->
            val uiText = when (restoreState) {
                DataRestoreState.IDLE -> null
                DataRestoreState.CHECKING_FOR_BACKUP -> UiText.StringResource(R.string.checking_for_backups)
                DataRestoreState.DOWNLOADING_DATA -> UiText.StringResource(R.string.downloading_app_data)
                DataRestoreState.PASSWORD_VERIFICATION -> UiText.StringResource(R.string.verify_your_password)
                DataRestoreState.RESTORE_IN_PROGRESS -> UiText.StringResource(R.string.data_restore_in_progress)
                DataRestoreState.COMPLETED -> UiText.StringResource(
                    R.string.restarting_app_in_time,
                    args = listOf(restartTimer.toString())
                )

                DataRestoreState.FAILED -> UiText.StringResource(R.string.error_app_data_restore_failed)
            }

            Text(
                text = uiText?.asString().orEmpty(),
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        SpacerSmall()

        Text(
            text = stringResource(R.string.data_restore_caution_message),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.large),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
    }
}

@Composable
private fun RestoreBackupActions(
    isBackupDownloaded: Boolean,
    isRestoreInProgress: Boolean,
    onRestoreClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonContainerColor = MaterialTheme.colorScheme.primaryContainer
    val buttonContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onRestoreClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonContainerColor,
                contentColor = buttonContentColor
            ),
            enabled = !isRestoreInProgress
        ) {
            Text(
                text = stringResource(
                    id = if (isBackupDownloaded) R.string.restore_backup
                    else R.string.check_for_backups
                )
            )
        }

        TextButton(
            onClick = onSkipClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current,
                disabledContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            ),
            enabled = !isRestoreInProgress
        ) {
            Text(stringResource(R.string.do_not_restore))
        }
    }
}

private const val DOWNLOAD_ICON_HEIGHT_FRACTION = 0.16f