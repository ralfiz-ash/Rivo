package dev.ridill.rivo.core.ui.navigation.destinations

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.presentation.util.rememberCredentialService
import dev.ridill.rivo.application.RUN_CONFIG_RESTORE_EXTRA
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberMultiplePermissionsLauncher
import dev.ridill.rivo.core.ui.components.rememberMultiplePermissionsState
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.util.findActivity
import dev.ridill.rivo.core.ui.util.restartApplication
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.onboarding.presentation.OnboardingScreen
import dev.ridill.rivo.onboarding.presentation.OnboardingViewModel

data object OnboardingScreenSpec : ScreenSpec {
    override val route: String
        get() = "onboarding"

    override val labelRes: Int
        get() = R.string.destination_onboarding

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: OnboardingViewModel = hiltViewModel(navBackStackEntry)
        val pagerState = rememberPagerState(
            pageCount = { OnboardingPage.entries.size }
        )
        val authState by viewModel.authState.collectAsState(initial = AuthState.UnAuthenticated)
        val restoreState by viewModel.dataRestoreState.collectAsStateWithLifecycle()
        val budgetInput = viewModel.budgetInput.collectAsStateWithLifecycle()
        val showEncryptionPasswordInput by viewModel.showEncryptionPasswordInput.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val activity = context.findActivity()

        val permissionsState = rememberMultiplePermissionsState(
            permissions = if (BuildUtil.isNotificationRuntimePermissionNeeded())
                listOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECEIVE_SMS)
            else listOf(Manifest.permission.RECEIVE_SMS),
            launcher = rememberMultiplePermissionsLauncher(
                onResult = viewModel::onPermissionsRequestResult
            )
        )

        val credentialService = rememberCredentialService(context)
        val currentPage by remember(pagerState) {
            derivedStateOf { pagerState.currentPage }
        }

        LaunchedEffect(currentPage) {
            viewModel.onPageChange(currentPage)
        }

        val authorizationResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
                result.data?.let(viewModel::onAuthorizationResult)
            }
        )

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is OnboardingViewModel.OnboardingEvent.NavigateToPage -> {
                    if (!pagerState.isScrollInProgress)
                        pagerState.animateScrollToPage(event.page.ordinal)
                }

                OnboardingViewModel.OnboardingEvent.LaunchNotificationPermissionRequest -> {
                    permissionsState.launchRequest()
                }

                is OnboardingViewModel.OnboardingEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                OnboardingViewModel.OnboardingEvent.OnboardingConcluded -> {
                    navController.navigate(DashboardScreenSpec.route) {
                        popUpTo(route) {
                            inclusive = true
                        }
                    }
                }

                OnboardingViewModel.OnboardingEvent.RestartApplication -> {
                    context.restartApplication(
                        editIntent = {
                            putExtra(RUN_CONFIG_RESTORE_EXTRA, true)
                        }
                    )
                }

                is OnboardingViewModel.OnboardingEvent.StartAutoSignInFlow -> {
                    val result = credentialService.startGetCredentialFlow(
                        filterByAuthorizedUsers = event.filterByAuthorizedAccounts,
                        activityContext = activity
                    )
                    viewModel.onCredentialResult(result)
                }

                is OnboardingViewModel.OnboardingEvent.StartAuthorizationFlow -> {
                    authorizationResultLauncher.launch(
                        Builder(event.pendingIntent).build()
                    )
                }

                OnboardingViewModel.OnboardingEvent.StartManualSignInFlow -> {
                    credentialService.startManualGetCredentialFlow(activity)
                }
            }
        }

        OnboardingScreen(
            snackbarController = snackbarController,
            pagerState = pagerState,
            permissionsState = permissionsState,
            authState = authState,
            restoreState = restoreState,
            showEncryptionPasswordInput = showEncryptionPasswordInput,
            budgetInput = { budgetInput.value },
            actions = viewModel
        )
    }
}