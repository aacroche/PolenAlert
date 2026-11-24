package com.aacroche.polenalert.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.dominantReading
import com.aacroche.polenalert.domain.usecase.GetMyPollenUseCase
import com.aacroche.polenalert.domain.usecase.MyPollenResult
import com.aacroche.polenalert.ui.today.TodayUiState.NoAllergies
import com.aacroche.polenalert.ui.today.TodayUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getMyPollen: GetMyPollenUseCase,
    private val clock: Clock
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Boolean>(replay = 1).apply { tryEmit(false) }

    val uiState: StateFlow<TodayUiState> = retryTrigger
        .flatMapLatest { force ->
            getMyPollen(force)
                .map { it.toUiState() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState.Loading)

    fun retry() {
        retryTrigger.tryEmit(true)
    }

    fun onLocationPermissionChange() {
        retryTrigger.tryEmit(false)
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
                val savedAt = this.fetchedAt?.atZone(clock.zone)?.toLocalDateTime()
                val savedHoursAgo =
                    this.fetchedAt?.let { Duration.between(it, clock.instant()).toHours() }

                Success(
                    today = visibleToday,
                    nextDays = this.days.drop(1),
                    date = now.toLocalDate(),
                    greeting = greetingFor(now.hour),
                    dominant = dominant,
                    name = this.name,
                    isOffline = this.isOffline,
                    savedAt = savedAt,
                    savedHoursAgo = savedHoursAgo,
                    isRefreshing = this.isRefreshing
                )
            }
        }

        is MyPollenResult.Error -> {
            TodayUiState.Error(this.error)
        }

        is MyPollenResult.NoAllergiesSelected -> {
            val now = LocalDateTime.now(clock)
            NoAllergies(
                date = now.toLocalDate(),
                greeting = greetingFor(now.hour),
                name = this.name
            )
        }

        MyPollenResult.Loading -> TodayUiState.Loading

        is MyPollenResult.LocationPermissionRequired -> {
            val now = LocalDateTime.now(clock)
            TodayUiState.NoLocation(now.toLocalDate(), greetingFor(now.hour), this.name)
        }

        is MyPollenResult.LocationUnavailable -> TodayUiState.LocationUnavailable

        is MyPollenResult.OutOfCoverage -> TodayUiState.OutOfCoverage
    }

    fun onResume() {
        if (uiState.value is TodayUiState.NoLocation) retryTrigger.tryEmit(false)
    }
}