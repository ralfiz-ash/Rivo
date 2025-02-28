package dev.ridill.rivo.onboarding.presentation

import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.onboarding.domain.model.DataRestoreState
import dev.ridill.rivo.onboarding.domain.model.SignInAndDataRestoreState
import java.util.Currency
import kotlin.time.Duration

data class OnboardingState(
    val signInAndDataRestoreState: SignInAndDataRestoreState = SignInAndDataRestoreState.SIGN_IN,
    val authState: AuthState = AuthState.UnAuthenticated,
    val dataRestoreState: DataRestoreState = DataRestoreState.IDLE,
    val showEncryptionPasswordInput: Boolean = false,
    val appRestartTimer: Duration = Duration.ZERO,
    val appCurrency: Currency = LocaleUtil.defaultCurrency
)