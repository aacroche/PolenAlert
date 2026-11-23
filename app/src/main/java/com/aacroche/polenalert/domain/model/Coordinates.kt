package com.aacroche.polenalert.domain.model

data class Coordinates(val latitude: Double, val longitude: Double)

fun Coordinates.isInPollenCoverage(): Boolean =
    latitude in 30.0..72.0 && longitude in -25.0..45.0
