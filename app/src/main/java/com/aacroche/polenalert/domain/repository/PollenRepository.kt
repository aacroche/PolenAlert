package com.aacroche.polenalert.domain.repository

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataResult

interface PollenRepository {
    suspend fun getDailyPollen(latitude: Double, longitude: Double): DataResult<List<DailyPollen>>
}