package com.aacroche.polenalert.data.repository

import com.aacroche.polenalert.data.local.PollenDao
import com.aacroche.polenalert.data.local.mappers.toDomain
import com.aacroche.polenalert.data.local.mappers.toEntities
import com.aacroche.polenalert.data.remote.PollenApi
import com.aacroche.polenalert.data.remote.mapper.AirQualityMapper.toDomain
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.repository.PollenRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.LocalDate

class PollenRepositoryImpl @Inject constructor(
    private val pollenApi: PollenApi,
    private val pollenDao: PollenDao,
    private val clock: Clock
) : PollenRepository {
    override fun observeDailyPollen(): Flow<List<DailyPollen>> =
        pollenDao.observeFrom(LocalDate.now(clock))
            .map {
                it.toDomain()
            }


    override suspend fun refresh(
        latitude: Double,
        longitude: Double
    ): DataResult<Unit> = try {
        val response = pollenApi.getPollen(latitude = latitude, longitude = longitude)
        pollenDao.replaceFrom(LocalDate.now(clock), response.toDomain().toEntities())
        DataResult.Success(Unit)
    } catch (e: IOException) {
        DataResult.Error(DataError.NO_CONNECTION)
    } catch (e: HttpException) {
        DataResult.Error(DataError.SERVER)
    } catch (e: SerializationException) {
        DataResult.Error(DataError.UNKNOWN)
    }
}
