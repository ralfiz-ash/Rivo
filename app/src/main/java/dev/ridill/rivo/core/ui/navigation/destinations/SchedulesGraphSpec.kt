package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

data object SchedulesGraphSpec : NavGraphSpec, BottomNavDestination {
    override val route: String
        get() = "schedules_graph"

    override val labelRes: Int
        get() = R.string.destination_schedules_graph

    override val iconRes: Int
        get() = R.drawable.ic_outline_schedule

    override val precedence: Int
        get() = 1

    override val children: List<NavDestination>
        get() = listOf(
        AllSchedulesScreenSpec
    )
}