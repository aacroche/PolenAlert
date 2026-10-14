package com.aacroche.polenalert.data.remote.mapper

import com.aacroche.polenalert.data.remote.dto.AirQualityResponseDto
import com.aacroche.polenalert.data.remote.dto.HourlyPollenDto
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import java.time.LocalDate

object AirQualityMapper {

    fun AirQualityResponseDto.toDomain(): List<DailyPollen> {
        return hourly.toDailyPollenList()
    }

    private fun HourlyPollenDto.toDailyPollenList(): List<DailyPollen> {
        val indicesByDate: Map<LocalDate, List<Int>> = time.indices.groupBy { index ->
            val dateString = time[index].take(10)
            LocalDate.parse(dateString)
        }

        return indicesByDate.map { (date, indices) ->
            val readings = PollenType.entries.mapNotNull { type ->
                createPollenReading(type = type, indices = indices)
            }
            DailyPollen(
                date = date.toString(),
                readings = readings
            )
        }
    }

    private fun HourlyPollenDto.createPollenReading(
        type: PollenType,
        indices: List<Int>
    ): PollenReading? {
        val pollenValues = valuesFor(type)
        val validValues = indices.mapNotNull { index -> pollenValues.getOrNull(index) }

        if (validValues.isEmpty()) return null

        val dailyMean = validValues.average()
        val level = type.levelFor(dailyMean)

        return PollenReading(
            type = type,
            level = level,
            dailyMean = dailyMean
        )
    }

    private fun HourlyPollenDto.valuesFor(type: PollenType): List<Double?> = when (type) {
        PollenType.ALDER -> alder
        PollenType.BIRCH -> birch
        PollenType.GRASS -> grass
        PollenType.MUGWORT -> mugwort
        PollenType.OLIVE -> olive
        PollenType.RAGWEED -> ragweed
    }
}