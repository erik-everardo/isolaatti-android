package com.isolaatti.squads.ui

import kotlinx.serialization.Serializable

sealed interface SquadMainNavigation {
    @Serializable
    data class Squad(val id: String) : SquadMainNavigation
    @Serializable
    data object SquadsHome : SquadMainNavigation
}

sealed interface SquadScreenRoute {
    @Serializable
    data object Home: SquadScreenRoute
    @Serializable
    data object Members: SquadScreenRoute
    @Serializable
    data object Settings: SquadScreenRoute
    @Serializable
    data object Invitations: SquadScreenRoute
    @Serializable
    data object Requests: SquadScreenRoute
}

@Serializable
sealed interface SquadsScreenRoute {
    @Serializable
    data object SquadsHome : SquadsScreenRoute
    @Serializable
    data object MySquads : SquadsScreenRoute
    @Serializable
    data object Invitations : SquadsScreenRoute
    @Serializable
    data object Requests : SquadsScreenRoute
}
