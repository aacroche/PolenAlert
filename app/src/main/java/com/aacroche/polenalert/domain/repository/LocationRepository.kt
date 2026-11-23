package com.aacroche.polenalert.domain.repository

import com.aacroche.polenalert.domain.model.Coordinates

interface LocationRepository {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): Coordinates?
}