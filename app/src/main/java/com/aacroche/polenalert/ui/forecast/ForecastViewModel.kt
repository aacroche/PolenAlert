package com.aacroche.polenalert.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.toForecasts
import com.aacroche.polenalert.domain.usecase.GetDailyPollenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val getDailyPollen: GetDailyPollenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _uiState.value = ForecastUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = getDailyPollen(MADRID_LAT, MADRID_LON)) {
                is DataResult.Success -> {
                    if (result.data.size < REQUIRED_DAYS) {
                        ForecastUiState.Empty
                    } else {
                        val dailyData = result.data.take(REQUIRED_DAYS)
                        val days = dailyData.mapNotNull { dailyPollen ->
                            runCatching { LocalDate.parse(dailyPollen.date) }.getOrNull()
                        }
                        val forecasts = dailyData.toForecasts()
                        if (forecasts.isEmpty()) {
                            ForecastUiState.Empty
                        } else {
                            ForecastUiState.Success(
                                days = days,
                                forecasts = forecasts
                            )
                        }
                    }
                }
                is DataResult.Error -> {
                    ForecastUiState.Error(result.error)
                }
            }
        }
    }

    private companion object {
        const val REQUIRED_DAYS = 4
        const val MADRID_LAT = 40.42
        const val MADRID_LON = -3.70
    }
}