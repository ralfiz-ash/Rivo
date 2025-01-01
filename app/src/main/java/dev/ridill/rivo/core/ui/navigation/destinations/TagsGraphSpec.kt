package dev.ridill.rivo.core.ui.navigation.destinations

import dev.ridill.rivo.R

data object TagsGraphSpec : NavGraphSpec {
    override val route: String
        get() = "tags_graph"

    override val labelRes: Int
        get() = R.string.destination_tags_graph

    override val children: List<NavDestination>
        get() = listOf(
            AllTagsScreenSpec
        )
}
