package com.aacroche.polenalert.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.aacroche.polenalert.R

enum class TopLevelDestination(
    val route: NavKey,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) {
    TODAY(TodayRoute, R.drawable.ic_house, R.string.tab_today),
    FORECAST(ForecastRoute, R.drawable.ic_calendar_days, R.string.tab_forecast),
    PROFILE(ProfileRoute, R.drawable.ic_user_round, R.string.tab_profile),
}