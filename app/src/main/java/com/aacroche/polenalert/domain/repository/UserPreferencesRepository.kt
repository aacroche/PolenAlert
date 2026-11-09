package com.aacroche.polenalert.domain.repository

import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setName(name: String)
    suspend fun setAllergy(type: PollenType, enabled: Boolean)
}