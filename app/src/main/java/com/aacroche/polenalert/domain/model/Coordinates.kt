package com.aacroche.polenalert.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(val latitude: Double, val longitude: Double)

fun Coordinates.isInPollenCoverage(): Boolean =
    latitude in 30.0..72.0 && longitude in -25.0..45.0

fun Coordinates.distanceKmTo(other: Coordinates): Double {
    val earthRadius = 6371.0
    val latDistance = Math.toRadians(other.latitude - latitude)
    val lonDistance = Math.toRadians(other.longitude - longitude)
    val a = sin(latDistance / 2).pow(2) +
            cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(lonDistance / 2).pow(
        2
    )
    return 2 * earthRadius * asin(sqrt(a))
}
