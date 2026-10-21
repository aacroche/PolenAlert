package com.aacroche.polenalert.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.usecase.GetDailyPollenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getDailyPollen: GetDailyPollenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _uiState.value = TodayUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = getDailyPollen(MADRID_LAT, MADRID_LON)) {
                is DataResult.Success -> {
                    val today = result.data.firstOrNull()
                    val visibleReadings = today?.readings.orEmpty().filter {
                        it.level != PollenLevel.NONE
                    }
                    if (today == null || visibleReadings.isEmpty()) {
                        TodayUiState.OutOfSeason
                    } else {
                        TodayUiState.Success(
                            today = today.copy(readings = visibleReadings),
                            nextDays = result.data.drop(1)
                        )
                    }
                }

                is DataResult.Error -> {
                    TodayUiState.Error(result.error)
                }
            }
        }
    }

    private companion object {
        const val MADRID_LAT = 40.42
        const val MADRID_LON = -3.70
    }
}