package com.aacroche.polenalert.domain.usecase

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

sealed interface MyPollenResult {
    val name: String

    data class NoAllergiesSelected(override val name: String) : MyPollenResult
    data class Data(override val name: String, val days: List<DailyPollen>) : MyPollenResult
    data class Error(override val name: String, val error: DataError) : MyPollenResult
}

const val LATITUDE = 40.42
const val LONGITUDE = -3.70

class GetMyPollenUseCase @Inject constructor(
    private val pollenRepository: PollenRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {


    operator fun invoke(): Flow<MyPollenResult> = flow {
        val refresh = pollenRepository.refresh(LATITUDE, LONGITUDE)
        emitAll(
            combine(
                pollenRepository.observeDailyPollen(),
                preferencesRepository.preferences
            ) { result, prefs ->
                when {
                    prefs.allergies.isEmpty() -> MyPollenResult.NoAllergiesSelected(prefs.name)
                    refresh is DataResult.Error -> MyPollenResult.Error(prefs.name, refresh.error)
                    else -> MyPollenResult.Data(
                        prefs.name,
                        result.filteredByAllergies(prefs.allergies)
                    )
                }
            }
        )
    }
}

fun List<DailyPollen>.filteredByAllergies(allergies: Set<PollenType>): List<DailyPollen> =
    this.map { day ->
        day.copy(readings = day.readings.filter { it.type in allergies })
    }
