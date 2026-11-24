package com.aacroche.polenalert.domain.model

data class Place(
    val name: String,
    val region: String?,              // admin1
    val country: String?,
    val coordinates: Coordinates,
)
