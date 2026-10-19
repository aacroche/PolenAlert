package com.aacroche.polenalert.data.remote

import com.aacroche.polenalert.data.remote.dto.AirQualityResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PollenApi {
    @GET("v1/air-quality")
    suspend fun getPollen(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = HOURLY_POLLEN,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 4
    ) : AirQualityResponseDto

    companion object {
        const val BASE_URL = "https://air-quality-api.open-meteo.com/"
        const val HOURLY_POLLEN =
            "alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen"
    }
}