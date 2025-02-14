package dev.ridill.rivo.settings.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.model.UserAccount
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.RivoImage
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.TitleMediumText
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun LoggedInAccountInfo(
    account: UserAccount,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        RivoImage(
            url = account.photoUrl,
            contentDescription = account.displayName,
            size = 24.dp
        )

        TitleMediumText(
            text = account.displayName
        )
    }
}

@Composable
fun AccountPreferenceInfo(
    authState: AuthState,
    onClick: () -> Unit,
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