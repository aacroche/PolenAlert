package com.aacroche.polenalert.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import androidx.datastore.preferences.core.Preferences

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            UserPreferences(
                name = prefs[NAME] ?: "",
                allergies = prefs[ALLERGIES].orEmpty()
                    .mapNotNull { runCatching { PollenType.valueOf(it) }.getOrNull() }
                    .toSet()
            )
        }

    override suspend fun setName(name: String) {
        dataStore.edit { it[NAME] = name.trim() }
    }

    override suspend fun setAllergy(type: PollenType, enabled: Boolean) {
        dataStore.edit {
            val current = it[ALLERGIES].orEmpty()
            it[ALLERGIES] = if (enabled) current + type.name else current - type.name
        }
    }

    private companion object {
        val NAME = stringPreferencesKey("name")
        val ALLERGIES = stringSetPreferencesKey("allergies")
    }
}