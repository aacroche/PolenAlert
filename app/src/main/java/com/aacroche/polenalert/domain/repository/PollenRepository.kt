package com.aacroche.polenalert.domain.repository

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface PollenRepository {
    fun observeDailyPollen(): Flow<List<DailyPollen>>
    suspend fun refresh(latitude: Double, longitude: Double, force: Boolean = false) : DataResult<Unit>
}