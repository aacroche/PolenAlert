package com.aacroche.polenalert.data.remote.dto

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class GeocodingResponseDto(
    val results: List<PlaceDto> = emptyList(),
)

@Serializable
data class PlaceDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
)
