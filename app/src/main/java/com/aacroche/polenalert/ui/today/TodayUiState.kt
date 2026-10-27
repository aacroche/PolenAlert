package com.aacroche.polenalert.ui.today

import androidx.annotation.StringRes
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import java.time.LocalDate
import java.time.LocalTime

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Success(
        val today: DailyPollen,
        val nextDays: List<DailyPollen>,
        val date: LocalDate,
        @StringRes val greeting: Int,
        val updatedAt: String
    ) : TodayUiState

    data object OutOfSeason : TodayUiState
    data class Error(val error: DataError) : TodayUiState
}