package com.aacroche.polenalert.data.repository

import com.aacroche.polenalert.data.remote.GeocodingApi
import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.Place
import com.aacroche.polenalert.domain.repository.GeocodingRepository
import jakarta.inject.Inject
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class GeocodingRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
) : GeocodingRepository {
    override suspend fun search(query: String, language: String): DataResult<List<Place>> {
        return try {
            val response = geocodingApi.searchPlaces(name = query, language = language)
            val places = response.results.map { dto ->
                Place(
                    name = dto.name,
                    region = dto.admin1,
                    country = dto.country,
                    coordinates = Coordinates(dto.latitude, dto.longitude),
                )
            }
            DataResult.Success(places)
        } catch (e: IOException) {
            DataResult.Error(DataError.NO_CONNECTION)
        } catch (e: HttpException) {
            DataResult.Error(DataError.SERVER)
        } catch (e: SerializationException) {
            DataResult.Error(DataError.UNKNOWN)
        }
    }
}
