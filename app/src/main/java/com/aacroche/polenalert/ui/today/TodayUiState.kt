package com.aacroche.polenalert.ui.today

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Success(val today: DailyPollen, val nextDays: List<DailyPollen>) : TodayUiState
    data object OutOfSeason : TodayUiState
    data class Error (val error: DataError) : TodayUiState
}