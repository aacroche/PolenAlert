package com.aacroche.polenalert.ui.forecast

import androidx.annotation.StringRes
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.PollenForecast
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface ForecastUiState {
    data object Loading : ForecastUiState
    data class Success(
        val days: List<LocalDate>,
        val forecasts: List<PollenForecast>,
        val isOffline: Boolean,
        val savedAt: LocalDateTime?,
        val savedHoursAgo: Long? = null
    ) : ForecastUiState

    data object Empty : ForecastUiState
    data object OutOfCoverage : ForecastUiState
    data object LocationUnavailable : ForecastUiState
    data class Error(val error: DataError) : ForecastUiState
    data class NoAllergies(val date: LocalDate, @StringRes val greeting: Int, val name: String) :
        ForecastUiState
    data class NoLocation(
        val date: LocalDate,
        @StringRes val greeting: Int,
        val name: String
    ) : ForecastUiState
}
