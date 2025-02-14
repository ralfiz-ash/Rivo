package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.account.presentation.accountDetails.AccountDetailsScreen
import dev.ridill.rivo.account.presentation.accountDetails.AccountDetailsViewModel
import dev.ridill.rivo.account.presentation.util.rememberCredentialService
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController

data object AccountDetailsScreenSpec : ScreenSpec {
    override val route: String
        get() = "account_details"

    override val labelRes: Int
        get() = R.string.destination_account_details

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: AccountDetailsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val credentialService = rememberCredentialService(context = context)
        val activity = LocalActivity.current
        CollectFlowEffect(viewModel.events, snackbarController, context, activity) { event ->
            when (event) {
                AccountDetailsViewModel.AccountDetailsEvent.AccountDeleted -> {
                    navController.navigateUp()
                }

                is AccountDetailsViewModel.AccountDetailsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                AccountDetailsViewModel.AccountDetailsEvent.StartManualSignInFlow -> {
                    activity?.let {
                        val result = credentialService.startManualGetCredentialFlow(
                            activityContext = it
                        )
                        viewModel.onCredentialResult(result)
                    }
                }
            }
        }

        AccountDetailsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}