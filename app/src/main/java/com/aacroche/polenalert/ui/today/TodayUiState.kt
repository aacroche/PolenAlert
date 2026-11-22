package com.aacroche.polenalert.ui.today

import androidx.annotation.StringRes
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.PollenReading
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Success(
        val today: DailyPollen,
        val nextDays: List<DailyPollen>,
        val date: LocalDate,
        @StringRes val greeting: Int,
        val dominant: PollenReading,
        val name: String,
        val isOffline: Boolean,
        val savedAt: LocalDateTime?,
        val savedHoursAgo: Long?,
        val isRefreshing: Boolean,
    ) : TodayUiState

    data object OutOfSeason : TodayUiState
    data class Error(val error: DataError) : TodayUiState
    data class NoAllergies(val date: LocalDate, @StringRes val greeting: Int, val name: String) :
        TodayUiState
}