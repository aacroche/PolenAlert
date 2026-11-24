package com.aacroche.polenalert.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.Place
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val lat = prefs[MANUAL_LAT]
            val lon = prefs[MANUAL_LON]
            UserPreferences(
                name = prefs[NAME] ?: "",
                allergies = prefs[ALLERGIES].orEmpty()
                    .mapNotNull { runCatching { PollenType.valueOf(it) }.getOrNull() }
                    .toSet(),
                manualPlace = if (lat != null && lon != null) {
                    Place(
                        name = prefs[MANUAL_NAME].orEmpty(),
                        region = prefs[MANUAL_REGION],
                        country = null,
                        coordinates = Coordinates(lat, lon),
                    )
                } else null
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

    override suspend fun setManualPlace(place: Place?) {
        dataStore.edit { prefs ->
            if (place == null) {
                prefs.remove(MANUAL_NAME)
                prefs.remove(MANUAL_REGION)
                prefs.remove(MANUAL_LAT)
                prefs.remove(MANUAL_LON)
            } else {
                prefs[MANUAL_NAME] = place.name
                prefs[MANUAL_LAT] = place.coordinates.latitude
                prefs[MANUAL_LON] = place.coordinates.longitude
                if (place.region != null) prefs[MANUAL_REGION] = place.region
                else prefs.remove(MANUAL_REGION)
            }
        }

    }

    private companion object {
        val NAME = stringPreferencesKey("name")
        val ALLERGIES = stringSetPreferencesKey("allergies")
        val MANUAL_NAME = stringPreferencesKey("manual_name")
        val MANUAL_REGION = stringPreferencesKey("manual_region")
        val MANUAL_LAT = doublePreferencesKey("manual_lat")
        val MANUAL_LON = doublePreferencesKey("manual_lon")
    }
}