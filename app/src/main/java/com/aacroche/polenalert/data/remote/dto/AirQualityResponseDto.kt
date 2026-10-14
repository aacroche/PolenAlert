package com.aacroche.polenalert.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.TimeZone

@Serializable
data class AirQualityResponseDto(
    var latitude: Double,
    var longitude: Double,
    var timezone: String,
    var hourly: HourlyPollenDto,
)

@Serializable
data class HourlyPollenDto(
    val time: List<String>,
    @SerialName("alder_pollen") val alder: List<Double?> = emptyList(),
    @SerialName("birch_pollen") val birch: List<Double?> = emptyList(),
    @SerialName("grass_pollen") val grass: List<Double?> = emptyList(),
    @SerialName("mugwort_pollen") val mugwort: List<Double?> = emptyList(),
    @SerialName("olive_pollen") val olive: List<Double?> = emptyList(),
    @SerialName("ragweed_pollen") val ragweed: List<Double?> = emptyList()
)

