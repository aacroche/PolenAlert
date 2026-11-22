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
import kotlinx.coroutines.flow.flow

sealed interface MyPollenResult {
    val name: String

    data class NoAllergiesSelected(override val name: String) : MyPollenResult
    data class Data(
        override val name: String,
        val days: List<DailyPollen>,
        val isOffline: Boolean
    ) : MyPollenResult

    data class Error(override val name: String, val error: DataError) : MyPollenResult
    data object Loading : MyPollenResult {
        override val name: String = ""
    }
}

const val LATITUDE = 46.0037
const val LONGITUDE = 8.9511

class GetMyPollenUseCase @Inject constructor(
    private val pollenRepository: PollenRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {


    operator fun invoke(force : Boolean = false): Flow<MyPollenResult> {
        val refreshState: Flow<DataResult<Unit>?> = flow {
            emit(null)
            emit(pollenRepository.refresh(LATITUDE, LONGITUDE, force))
        }
        return combine(
            pollenRepository.observeDailyPollen(),
            preferencesRepository.preferences,
            refreshState
        ) { result, prefs, refresh ->
            when {
                result.isEmpty() && refresh == null -> MyPollenResult.Loading
                result.isEmpty() && refresh is DataResult.Error -> MyPollenResult.Error(
                    prefs.name,
                    refresh.error
                ) //first dataResult from api error
                prefs.allergies.isEmpty() -> MyPollenResult.NoAllergiesSelected(prefs.name)
                else -> MyPollenResult.Data(
                    name = prefs.name,
                    days = result.filteredByAllergies(prefs.allergies),
                    isOffline = refresh is DataResult.Error
                )
            }
        }
    }
}

fun List<DailyPollen>.filteredByAllergies(allergies: Set<PollenType>): List<DailyPollen> =
    this.map { day ->
        day.copy(readings = day.readings.filter { it.type in allergies })
    }
