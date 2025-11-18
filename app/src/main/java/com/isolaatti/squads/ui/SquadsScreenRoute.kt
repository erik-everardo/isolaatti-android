package com.isolaatti.squads.ui

import kotlinx.serialization.Serializable

sealed interface SquadMainNavigation {
    @Serializable
    data class Squad(val id: String) : SquadMainNavigation
    @Serializable
    data object SquadsHome : SquadMainNavigation

    @Serializable
    data class SquadsSettings(val id: String) : SquadMainNavigation
}

sealed interface SquadScreenRoute {
    @Serializable
    data object Readme: SquadScreenRoute
    @Serializable
    data object Members: SquadScreenRoute
    @Serializable
    data object Posts: SquadScreenRoute
}
