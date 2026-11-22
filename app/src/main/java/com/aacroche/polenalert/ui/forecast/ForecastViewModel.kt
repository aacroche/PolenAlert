package com.aacroche.polenalert.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.toForecasts
import com.aacroche.polenalert.domain.usecase.GetMyPollenUseCase
import com.aacroche.polenalert.domain.usecase.MyPollenResult
import com.aacroche.polenalert.ui.forecast.ForecastUiState.NoAllergies
import com.aacroche.polenalert.ui.forecast.ForecastUiState.Success
import com.aacroche.polenalert.ui.today.greetingFor
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val getMyPollen: GetMyPollenUseCase,
    private val clock: Clock
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Boolean>(replay = 1).apply { tryEmit(false) }

    val uiState: StateFlow<ForecastUiState> = retryTrigger
        .flatMapLatest { force ->
            getMyPollen(force)
                .map { it.toUiState() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ForecastUiState.Loading)

    fun retry() {
        retryTrigger.tryEmit(true)
    }

    private fun MyPollenResult.toUiState(): ForecastUiState = when (this) {
        is MyPollenResult.Data -> {
            if (this.days.size < REQUIRED_DAYS) {
                ForecastUiState.Empty
            } else {
                val dailyData = this.days.take(REQUIRED_DAYS)
                val days = dailyData.map { dailyPollen ->
                    LocalDate.parse(dailyPollen.date)
                }
                val forecasts = dailyData.toForecasts()
                if (forecasts.isEmpty()) {
                    ForecastUiState.Empty
                } else {
                    Success(
                        days = days,
                        forecasts = forecasts,
                        isOffline = this.isOffline
                    )
                }
            }
        }

        is MyPollenResult.Error -> {
            ForecastUiState.Error(this.error)
        }

        is MyPollenResult.NoAllergiesSelected -> {
            val now = LocalDateTime.now(clock)
            NoAllergies(
                date = now.toLocalDate(),
                greeting = greetingFor(now.hour),
                name = this.name
            )
        }

        MyPollenResult.Loading -> ForecastUiState.Loading
    }

    private companion object {
        const val REQUIRED_DAYS = 4
        const val MADRID_LAT = 40.42
        const val MADRID_LON = -3.70
    }
}