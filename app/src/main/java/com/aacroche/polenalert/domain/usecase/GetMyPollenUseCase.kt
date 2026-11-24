package com.aacroche.polenalert.domain.usecase

import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.isInPollenCoverage
import com.aacroche.polenalert.domain.repository.LocationRepository
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant


sealed interface MyPollenResult {
    val name: String

    data class NoAllergiesSelected(override val name: String) : MyPollenResult
    data class Data(
        override val name: String,
        val days: List<DailyPollen>,
        val isOffline: Boolean,
        val fetchedAt: Instant? = null,
        val isRefreshing: Boolean = false,
    ) : MyPollenResult

    data class Error(override val name: String, val error: DataError) : MyPollenResult
    data object Loading : MyPollenResult { override val name: String = "" }
    data class LocationPermissionRequired(override val name: String) : MyPollenResult
    data class LocationUnavailable(override val name: String) : MyPollenResult
    data class OutOfCoverage(override val name: String) : MyPollenResult
}

class GetMyPollenUseCase @Inject constructor(
    private val pollenRepository: PollenRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val locationRepository: LocationRepository,
) {

    operator fun invoke(force: Boolean = false): Flow<MyPollenResult> = flow {
        if (!locationRepository.hasLocationPermission()) {
            emitAll(preferencesRepository.preferences.map { MyPollenResult.LocationPermissionRequired(it.name) })
            return@flow
        }

        val coordinates = locationRepository.getCurrentLocation()
        when {
            coordinates == null ->
                emitAll(preferencesRepository.preferences.map { MyPollenResult.LocationUnavailable(it.name) })

            !coordinates.isInPollenCoverage() ->
                emitAll(preferencesRepository.preferences.map { MyPollenResult.OutOfCoverage(it.name) })

            else ->
                emitAll(pollenFor(coordinates, force))
        }
    }

    private fun pollenFor(coordinates: Coordinates, force: Boolean): Flow<MyPollenResult> {
        val refreshState: Flow<DataResult<Unit>?> = flow {
            emit(null) // null = refrescando
            emit(pollenRepository.refresh(coordinates, force))
        }

        return combine(
            pollenRepository.observeDailyPollen(coordinates),
            preferencesRepository.preferences,
            refreshState,
        ) { snapshot, prefs, refresh ->
            val days = snapshot.days
            when {
                days.isEmpty() && refresh == null -> MyPollenResult.Loading
                days.isEmpty() && refresh is DataResult.Error -> MyPollenResult.Error(prefs.name, refresh.error)
                prefs.allergies.isEmpty() -> MyPollenResult.NoAllergiesSelected(prefs.name)
                else -> MyPollenResult.Data(
                    name = prefs.name,
                    days = days.filteredByAllergies(prefs.allergies),
                    isOffline = refresh is DataResult.Error,
                    fetchedAt = snapshot.fetchedAt,
                    isRefreshing = refresh == null,
                )
            }
        }
    }
}

fun List<DailyPollen>.filteredByAllergies(allergies: Set<PollenType>): List<DailyPollen> =
    map { day -> day.copy(readings = day.readings.filter { it.type in allergies }) }