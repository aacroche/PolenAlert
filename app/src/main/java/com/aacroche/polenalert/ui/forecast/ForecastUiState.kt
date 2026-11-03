package com.aacroche.polenalert.ui.forecast

import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.PollenForecast
import java.time.LocalDate

sealed interface ForecastUiState {
    data object Loading : ForecastUiState
    data class Success(val days: List<LocalDate>, val forecasts: List<PollenForecast>) : ForecastUiState
    data object Empty : ForecastUiState
    data class Error(val error: DataError) : ForecastUiState
}