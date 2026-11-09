package com.aacroche.polenalert.domain.usecase

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

sealed interface MyPollenResult {
    val name: String

    data class NoAllergiesSelected(override val name: String) : MyPollenResult
    data class Data(override val name: String, val days: List<DailyPollen>) : MyPollenResult
    data class Error(override val name: String, val error: DataError) : MyPollenResult
}

class GetMyPollenUseCase @Inject constructor(
    private val pollenRepository: PollenRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(latitude: Double, longitude: Double): Flow<MyPollenResult> {
        val pollen = flow { emit(pollenRepository.getDailyPollen(latitude, longitude)) }

        return combine(pollen, preferencesRepository.preferences) { result, prefs ->
            when (result) {
                is DataResult.Error -> MyPollenResult.Error(prefs.name, result.error)
                is DataResult.Success -> {
                    if (prefs.allergies.isEmpty()) {
                        MyPollenResult.NoAllergiesSelected(prefs.name)
                    } else {
                        val filteredDays = result.data.map { day ->
                            day.copy(readings = day.readings.filter { it.type in prefs.allergies })
                        }
                        MyPollenResult.Data(prefs.name, filteredDays)
                    }
                }
            }
        }
    }
}