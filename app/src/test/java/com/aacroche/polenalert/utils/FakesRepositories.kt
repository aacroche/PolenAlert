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
    val days = MutableStateFlow<List<DailyPollen>>(emptyList())
    var refreshResult: DataResult<Unit> = DataResult.Success(Unit)
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
                }
            }
        }

    override fun observeDailyPollen() = days
    override suspend fun refresh(latitude: Double, longitude: Double): DataResult<Unit> {
        calls++
        return refreshResult
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
