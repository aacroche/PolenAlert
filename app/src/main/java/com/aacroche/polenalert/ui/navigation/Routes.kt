package com.aacroche.polenalert.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TodayRoute : NavKey

@Serializable
data object ForecastRoute : NavKey

@Serializable
data object ProfileRoute : NavKey