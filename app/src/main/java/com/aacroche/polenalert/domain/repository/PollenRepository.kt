package com.aacroche.polenalert.domain.repository

import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenSnapshot
import kotlinx.coroutines.flow.Flow

interface PollenRepository {
    fun observeDailyPollen(coordinates: Coordinates): Flow<PollenSnapshot>
    suspend fun refresh(coordinates: Coordinates, force: Boolean = false) : DataResult<Unit>
}