package com.aacroche.polenalert.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.dominantReading
import com.aacroche.polenalert.domain.usecase.GetMyPollenUseCase
import com.aacroche.polenalert.domain.usecase.MyPollenResult
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getMyPollen: GetMyPollenUseCase,
    private val clock: Clock
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<TodayUiState> = retryTrigger
        .flatMapLatest {
            getMyPollen(MADRID_LAT, MADRID_LON)
                .map { it.toUiState() }
                .onStart { emit(TodayUiState.Loading) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState.Loading)

    fun retry() {
        retryTrigger.tryEmit(Unit)
    }

    private fun MyPollenResult.toUiState(): TodayUiState = when (this) {
        is MyPollenResult.Data -> {
            val today = this.days.firstOrNull()
            val visibleReadings = today?.readings.orEmpty().filter {
                it.level != PollenLevel.NONE
            }
            val visibleToday = today?.copy(readings = visibleReadings)
            val dominant = visibleToday?.dominantReading()

            if (today == null || visibleReadings.isEmpty() || dominant == null) {
                TodayUiState.OutOfSeason
            } else {
                val now = LocalDateTime.now(clock)
                val format = DateTimeFormatter.ofPattern("HH:mm")

                TodayUiState.Success(
                    today = visibleToday,
                    nextDays = this.days.drop(1),
                    date = now.toLocalDate(),
                    greeting = greetingFor(now.hour),
                    updatedAt = now.format(format),
                    dominant = dominant,
                    name = this.name
                )
            }
        }

        is MyPollenResult.Error -> {
            TodayUiState.Error(this.error)
        }

        is MyPollenResult.NoAllergiesSelected -> {
            val now = LocalDateTime.now(clock)
            TodayUiState.NoAllergies(
                date = now.toLocalDate(),
                greeting = greetingFor(now.hour),
                name = this.name
            )
        }
    }

    private companion object {
        const val MADRID_LAT = 40.42
        const val MADRID_LON = -3.70
    }
}