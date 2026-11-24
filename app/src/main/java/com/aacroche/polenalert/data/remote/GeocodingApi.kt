package com.aacroche.polenalert.data.remote

import com.aacroche.polenalert.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun searchPlaces(
        @Query("name") name: String,
        @Query("language") language: String,
        @Query("count") count: Int = 5,
    ): GeocodingResponseDto

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}