package dev.ridill.rivo.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.animPreferences.AnimPreferencesManager
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTxResult
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: DashboardRepository,
    private val animPreferencesManager: AnimPreferencesManager,
    private val notificationHelper: NotificationHelper<Transaction>,
    private val eventBus: EventBus<DashboardEvent>
) : ViewModel() {
    private val signedInUsername = repo.getUsername()

    private val budget = repo.getCurrentBudget()
    private val totalDebit = repo.getTotalDebitsForCurrentMonth()
    private val totalCredit = repo.getTotalCreditsForCurrentMonth()

    private val budgetInclCredits = combineTuple(
        budget,
        totalCredit
    ).map { (budget, credit) ->
        budget + credit
    }.distinctUntilChanged()

    private val balance = combineTuple(
        budgetInclCredits,
        totalDebit
    ).map { (budgetInclCredits, debits) ->
        budgetInclCredits - debits
    }.distinctUntilChanged()

    private val activeSchedules = repo.getSchedulesActiveThisMonth()

    val recentSpendsPagingData = repo.getRecentSpends()
        .cachedIn(viewModelScope)

    private val showRecentSpendMarqueeTooltip = animPreferencesManager.preferences
        .mapLatest { it.showDashboardRecentSpendMarqueeTooltip }
        .distinctUntilChanged()

    val state = combineTuple(
        budgetInclCredits,
        totalDebit,
        totalCredit,
        balance,
        activeSchedules,
        signedInUsername,
        showRecentSpendMarqueeTooltip
    ).map { (
                budgetInclCredits,
                spentAmount,
                creditAmount,
                balance,
                activeSchedules,
                signedInUsername,
                showRecentSpendMarqueeTooltip
            ) ->
        DashboardState(
            balance = balance,
            spentAmount = spentAmount,
            creditAmount = creditAmount,
            monthlyBudgetInclCredits = budgetInclCredits,
            activeSchedules = activeSchedules,
            signedInUsername = signedInUsername,
            showRecentSpendMarqueeTooltip = showRecentSpendMarqueeTooltip
        )
    }.asStateFlow(viewModelScope, DashboardState())

    val events = eventBus.eventFlow

    init {
        cancelNotifications()
    }

    fun refreshCurrentDate() = repo.refreshCurrentDate()

    fun onNavResult(result: AddEditTxResult) = viewModelScope.launch {
        val event = when (result) {
            AddEditTxResult.TRANSACTION_DELETED ->
                DashboardEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_deleted))

            AddEditTxResult.TRANSACTION_SAVED ->
                DashboardEvent.ShowUiMessage(UiText.StringResource(R.string.transaction_saved))

            AddEditTxResult.SCHEDULE_SAVED -> DashboardEvent.ScheduleSaved
        }

        eventBus.send(event)
    }

    private fun cancelNotifications() {
        notificationHelper.dismissAllNotifications()
    }

    fun onRecentSpendClick() {
        viewModelScope.launch {
            animPreferencesManager.disableDashboardRecentSpendMarqueeTooltip()
        }
    }

    sealed interface DashboardEvent {
        data class ShowUiMessage(val uiText: UiText) : DashboardEvent
        data object ScheduleSaved : DashboardEvent
    }
}