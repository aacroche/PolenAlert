package com.aacroche.polenalert.data.repository

import com.aacroche.polenalert.data.remote.PollenApi
import com.aacroche.polenalert.data.remote.mapper.AirQualityMapper.toDomain
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.repository.PollenRepository
import jakarta.inject.Inject
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PollenRepositoryImpl @Inject constructor(private val pollenApi: PollenApi) : PollenRepository {
    override suspend fun getDailyPollen(
        latitude: Double,
        longitude: Double
    ): DataResult<List<DailyPollen>> {
        return try {
            val response = pollenApi.getPollen(latitude = latitude, longitude = longitude)
            DataResult.Success(response.toDomain())
        } catch (e: IOException) {
            DataResult.Error(DataError.NO_CONNECTION)
        } catch (e: HttpException) {
            DataResult.Error(DataError.SERVER)
        } catch (e: SerializationException) {
            DataResult.Error(DataError.UNKNOWN)
        }
    }
}
