package com.aacroche.polenalert.mapper

import com.aacroche.polenalert.data.remote.dto.AirQualityResponseDto
import com.aacroche.polenalert.data.remote.dto.HourlyPollenDto
import com.aacroche.polenalert.data.remote.mapper.AirQualityMapper.toDomain
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenType
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AirQualityMapperTest {
    private fun dto(
        time: List<String>,
        grass: List<Double?> = emptyList(),
        olive: List<Double?> = emptyList(),
        alder: List<Double?> = emptyList(),
        birch: List<Double?> = emptyList(),
        mugwort: List<Double?> = emptyList(),
        ragweed: List<Double?> = emptyList(),
    ) = AirQualityResponseDto(
        latitude = 40.42,
        longitude = -3.70,
        timezone = "Europe/Madrid",
        hourly = HourlyPollenDto(
            time = time,
            alder = alder,
            birch = birch,
            grass = grass,
            mugwort = mugwort,
            olive = olive,
            ragweed = ragweed
        ),
    )

    @Test
    fun `hourly values are averaged per day`() {
        val result = dto(
            time = listOf("2026-10-14T00:00", "2026-10-14T01:00", "2026-10-15T00:00"),
            grass = listOf(20.0, 40.0, 10.0)
        ).toDomain()

        assertEquals(2, result.size)
        val day1 = result.first().readings.single { it.type == PollenType.GRASS }
        assertEquals(30.0, day1.dailyMean, 0.001)
        assertEquals(PollenLevel.MEDIUM, day1.level)
    }

    @Test
    fun `null values ignored in average`() {
        val result = dto(
            time = listOf("2026-10-14T00:00", "2026-10-14T01:00", "2026-10-15T00:00"),
            grass = listOf(20.0, null, 10.0)
        ).toDomain()

        assertEquals(2, result.size)
        val day1 = result.first().readings.single { it.type == PollenType.GRASS }
        assertEquals(20.0, day1.dailyMean, 0.001)
        assertEquals(PollenLevel.MEDIUM, day1.level)
    }

    @Test
    fun `pollen without values does not appear in daily readings`() {
        val result = dto(
            time = listOf("2026-10-14T00:00"),
            grass = listOf(10.0),
            olive = emptyList()
        ).toDomain()

        assertEquals(1, result.size)
        val readings = result.first().readings
        assertEquals(1, readings.size)
        assertEquals(PollenType.GRASS, readings.first().type)
    }

    @Test
    fun `empty response returns empty list`() {
        val result = dto(time = emptyList()).toDomain()

        assertEquals(0, result.size)
    }

    @Test
    fun `levelFor boundary values for grass and olive`() {
        assertEquals(PollenLevel.NONE, PollenType.GRASS.levelFor(0.5))
        assertEquals(PollenLevel.LOW, PollenType.GRASS.levelFor(15.0))
        assertEquals(PollenLevel.MEDIUM, PollenType.GRASS.levelFor(15.1))

        assertEquals(PollenLevel.MEDIUM, PollenType.OLIVE.levelFor(100.0))
        assertEquals(PollenLevel.HIGH, PollenType.OLIVE.levelFor(100.1))
    }
}
