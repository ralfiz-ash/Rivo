package dev.ridill.rivo.settings.presentation.backupEncryption

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SmallDisplayText
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.autofill
import dev.ridill.rivo.core.ui.navigation.destinations.BackupEncryptionScreenSpec
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun BackupEncryptionScreen(
    snackbarController: SnackbarController,
    currentPassword: () -> String,
    newPassword: () -> String,
    confirmNewPassword: () -> String,
    state: BackupEncryptionState,
    actions: BackupEncryptionActions,
    navigateUp: () -> Unit
) {
    RivoScaffold(
        isLoading = state.isLoading,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        },
        snackbarController = snackbarController,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(MaterialTheme.spacing.medium)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudDone,
                contentDescription = null,
                modifier = Modifier
                    .size(IconSize)
            )
            SmallDisplayText(stringResource(BackupEncryptionScreenSpec.labelRes))

            SpacerMedium()

            Text(
                text = stringResource(R.string.backup_encryption_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.large)
            )

            Spacer(weight = Float.One)

            OutlinedButton(
                onClick = actions::onUpdatePasswordClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.update_password))
            }
        }

        if (state.showPasswordInput) {
            PasswordUpdateSheet(
                hasExistingPassword = state.hasExistingPassword,
                onDismissRequest = actions::onPasswordInputDismiss,
                currentPassword = currentPassword,
                onCurrentPasswordChange = actions::onCurrentPasswordChange,
                onForgotPasswordClick = actions::onForgotCurrentPasswordClick,
                newPassword = newPassword,
                onNewPasswordChange = actions::onNewPasswordChange,
                confirmNewPassword = confirmNewPassword,
                onConfirmNewPasswordChange = actions::onConfirmNewPasswordChange,
                isLoading = state.isPasswordUpdateButtonLoading,
                onConfirmClick = actions::onPasswordUpdateConfirm
            )
        }
    }
}

private val IconSize = 80.dp

@Composable
private fun PasswordUpdateSheet(
    hasExistingPassword: Boolean,
    onDismissRequest: () -> Unit,
    currentPassword: () -> String,
    onCurrentPasswordChange: (String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    newPassword: () -> String,
    onNewPasswordChange: (String) -> Unit,
    confirmNewPassword: () -> String,
    onConfirmNewPasswordChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val confirmEnabled by remember(hasExistingPassword) {
        derivedStateOf {
            (!hasExistingPassword || currentPassword().isNotEmpty())
                    && newPassword().isNotEmpty()
                    && confirmNewPassword().isNotEmpty()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            MediumDisplayText(text = stringResource(R.string.update_password))
            AnimatedVisibility(hasExistingPassword) {
                Column {
                    PasswordField(
                        value = currentPassword,
                        onValueChange = onCurrentPasswordChange,
                        labelRes = R.string.current_password,
                        modifier = Modifier
                            .fillMaxWidth()
                            .autofill(
                                types = listOf(AutofillType.Password),
                                onFill = onCurrentPasswordChange
                            )
                    )
                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier
                            .align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.forgot_password))
                    }
                }
            }

            PasswordField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                labelRes = R.string.new_password,
                modifier = Modifier
                    .fillMaxWidth()
                    .autofill(
                        types = listOf(AutofillType.NewPassword),
                        onFill = onNewPasswordChange
                    )
            )

            PasswordField(
                value = confirmNewPassword,
                onValueChange = onConfirmNewPasswordChange,
                labelRes = R.string.confirm_new_password,
                modifier = Modifier
                    .fillMaxWidth()
                    .autofill(
                        types = listOf(AutofillType.NewPassword),
                        onFill = onConfirmNewPasswordChange
                    ),
                imeAction = ImeAction.Done
            )

            ButtonWithLoadingIndicator(
                textRes = R.string.action_confirm,
                loading = isLoading,
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = confirmEnabled
            )
        }
    }
}

@Composable
private fun PasswordField(
    @StringRes labelRes: Int,
    value: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    TextField(
        value = value(),
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}