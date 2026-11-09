package com.aacroche.polenalert.domain.usecase

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.repository.PollenRepository
import jakarta.inject.Inject

class GetDailyPollenUseCase @Inject constructor(
    private val pollenRepository: PollenRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): DataResult<List<DailyPollen>> {
        return pollenRepository.getDailyPollen(latitude, longitude)
    }
}
