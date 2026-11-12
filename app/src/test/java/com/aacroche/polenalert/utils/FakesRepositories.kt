package com.aacroche.polenalert.utils

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePollenRepository : PollenRepository {
    var result: DataResult<List<DailyPollen>> = DataResult.Success(emptyList())
    var calls = 0

    override suspend fun getDailyPollen(latitude: Double, longitude: Double): DataResult<List<DailyPollen>> {
        calls++
        return result
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val _preferences = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = _preferences

    fun setPreferences(prefs: UserPreferences) {
        _preferences.value = prefs
    }

    override suspend fun setName(name: String) {
        _preferences.value = _preferences.value.copy(name = name)
    }

    override suspend fun setAllergy(type: PollenType, enabled: Boolean) {
        val currentAllergies = _preferences.value.allergies.toMutableSet()
        if (enabled) currentAllergies.add(type) else currentAllergies.remove(type)
        _preferences.value = _preferences.value.copy(allergies = currentAllergies)
    }
}
