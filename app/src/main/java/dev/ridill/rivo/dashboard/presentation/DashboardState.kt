package dev.ridill.rivo.dashboard.presentation

import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule

data class DashboardState(
    val balance: Double = Double.Zero,
    val spentAmount: Double = Double.Zero,
    val creditAmount: Double = Double.Zero,
    val monthlyBudgetInclCredits: Double = Double.Zero,
    val activeSchedules: List<ActiveSchedule> = emptyList(),
    val signedInUsername: String? = null,
    val showRecentSpendMarqueeTooltip: Boolean = true
)