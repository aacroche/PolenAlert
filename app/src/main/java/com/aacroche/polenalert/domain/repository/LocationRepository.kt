package com.aacroche.polenalert.domain.repository

interface LocationRepository {
    fun hasLocationPermission(): Boolean
}