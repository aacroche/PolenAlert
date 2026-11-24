package com.aacroche.polenalert.utils

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.Place
import com.aacroche.polenalert.domain.model.PollenSnapshot
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.domain.repository.GeocodingRepository
import com.aacroche.polenalert.domain.repository.LocationRepository
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.Instant

class FakePollenRepository : PollenRepository {
    val days = MutableStateFlow<List<DailyPollen>>(emptyList())
    val fetchedAt = MutableStateFlow<Instant?>(null)
    var refreshResult: DataResult<Unit> = DataResult.Success(Unit)
    var refreshGate: CompletableDeferred<Unit>? = null
    var calls = 0

    var result: DataResult<List<DailyPollen>>
        get() = when (val res = refreshResult) {
            is DataResult.Error -> DataResult.Error(res.error)
            is DataResult.Success -> DataResult.Success(days.value)
        }
        set(value) {
            when (value) {
                is DataResult.Success -> {
                    refreshResult = DataResult.Success(Unit)
                    days.value = value.data
                }
                is DataResult.Error -> {
                    refreshResult = DataResult.Error(value.error)
                    days.value = emptyList()
                    fetchedAt.value = null
                }
            }
        }

    override fun observeDailyPollen(coordinates: com.aacroche.polenalert.domain.model.Coordinates): Flow<PollenSnapshot> =
        combine(days, fetchedAt) { d, f -> PollenSnapshot(d, f) }

    override suspend fun refresh(coordinates: com.aacroche.polenalert.domain.model.Coordinates, force: Boolean): DataResult<Unit> {
        calls++
        refreshGate?.await()
        return refreshResult
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val _preferences = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = _preferences

    var lastSavedName: String? = null

    override suspend fun setName(name: String) {
        lastSavedName = name
        _preferences.value = _preferences.value.copy(name = name)
    }

    fun setPreferences(prefs: UserPreferences) {
        _preferences.value = prefs
    }

    override suspend fun setAllergy(type: PollenType, enabled: Boolean) {
        val currentAllergies = _preferences.value.allergies.toMutableSet()
        if (enabled) currentAllergies.add(type) else currentAllergies.remove(type)
        _preferences.value = _preferences.value.copy(allergies = currentAllergies)
    }

    override suspend fun setManualPlace(place: Place?) {
        _preferences.value = _preferences.value.copy(manualPlace = place)
    }
}

class FakeLocationRepository(
    var hasPermission: Boolean = true,
    var location: com.aacroche.polenalert.domain.model.Coordinates? = com.aacroche.polenalert.domain.model.Coordinates(40.4168, -3.7038)
) : LocationRepository {
    override fun hasLocationPermission(): Boolean = hasPermission
    override suspend fun getCurrentLocation(): com.aacroche.polenalert.domain.model.Coordinates? = location
}

class FakeGeocodingRepository : GeocodingRepository {
    var calls = 0
    var lastQuery: String? = null
    var result: DataResult<List<Place>> = DataResult.Success(emptyList())

    override suspend fun search(query: String, language: String): DataResult<List<Place>> {
        calls++
        lastQuery = query
        return result
    }
}

