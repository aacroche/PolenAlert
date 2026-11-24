package com.aacroche.polenalert.data.repository

import com.aacroche.polenalert.data.local.PollenDao
import com.aacroche.polenalert.data.local.mappers.toEntities
import com.aacroche.polenalert.data.local.mappers.toSnapshot
import com.aacroche.polenalert.data.remote.PollenApi
import com.aacroche.polenalert.data.remote.mapper.AirQualityMapper.toDomain
import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenSnapshot
import com.aacroche.polenalert.domain.model.distanceKmTo
import com.aacroche.polenalert.domain.repository.PollenRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class PollenRepositoryImpl @Inject constructor(
    private val pollenApi: PollenApi,
    private val pollenDao: PollenDao,
    private val clock: Clock
) : PollenRepository {

    private val refreshMutex = Mutex()
    private companion object {
        val MAX_CACHE_AGE: Duration = Duration.ofHours(1)
        const val MAX_DISTANCE_KM = 5.0
    }

    override fun observeDailyPollen(coordinates: Coordinates): Flow<PollenSnapshot> =
        pollenDao.observeFrom(LocalDate.now(clock))
            .map { rows ->
                val cachedAt = rows.firstOrNull()?.let {
                    Coordinates(it.latitude, it.longitude)
                }
                if(cachedAt == null || cachedAt.distanceKmTo(coordinates) >= MAX_DISTANCE_KM) {
                    PollenSnapshot(days = emptyList(), fetchedAt = null)
                } else {
                    rows.toSnapshot()
                }
            }


    override suspend fun refresh(
        coordinates: Coordinates,
        force: Boolean
    ): DataResult<Unit> =
        refreshMutex.withLock {
            val today = LocalDate.now(clock)
            val now = clock.instant()
            val lastFetch = pollenDao.oldestFetchedAt(today)?.let(Instant::ofEpochMilli)
            val cachedAt = pollenDao.cachedLocation(today)?.let { Coordinates(it.latitude, it.longitude) }
            val isFresh = lastFetch != null && Duration.between(lastFetch, now) < MAX_CACHE_AGE
            val isNearby = cachedAt != null && cachedAt.distanceKmTo(coordinates) < MAX_DISTANCE_KM


            if (!force && isFresh && isNearby) return@withLock DataResult.Success(Unit)

            try {
                val response = pollenApi.getPollen(latitude = coordinates.latitude, longitude = coordinates.longitude)
                pollenDao.replaceAll(response.toDomain().toEntities(now, coordinates))
                DataResult.Success(Unit)
            } catch (e: IOException) {
                DataResult.Error(DataError.NO_CONNECTION)
            } catch (e: HttpException) {
                DataResult.Error(DataError.SERVER)
            } catch (e: SerializationException) {
                DataResult.Error(DataError.UNKNOWN)
            }
        }
}
