package com.aacroche.polenalert.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TrendTest {

    @Test
    fun `toTrend evaluates rising, falling, and stable correctly`() {
        assertEquals(Trend.RISING, listOf(PollenLevel.LOW, PollenLevel.HIGH).toTrend())
        assertEquals(Trend.FALLING, listOf(PollenLevel.HIGH, PollenLevel.LOW).toTrend())
        assertEquals(Trend.STABLE, listOf(PollenLevel.MEDIUM, PollenLevel.MEDIUM).toTrend())
        assertEquals(Trend.STABLE, emptyList<PollenLevel>().toTrend())
    }

    @Test
    fun `toForecasts creates one PollenForecast per unique PollenType with correct daily levels`() {
        val day1 = DailyPollen(
            date = "2023-05-01",
            readings = listOf(
                PollenReading(PollenType.ALDER, PollenLevel.LOW, 10.0),
                PollenReading(PollenType.GRASS, PollenLevel.MEDIUM, 30.0)
            )
        )
        val day2 = DailyPollen(
            date = "2023-05-02",
            readings = listOf(
                PollenReading(PollenType.ALDER, PollenLevel.HIGH, 60.0),
                PollenReading(PollenType.GRASS, PollenLevel.LOW, 15.0)
            )
        )

        val forecasts = listOf(day1, day2).toForecasts()

        assertEquals(2, forecasts.size)
        // Verify types are unique
        assertEquals(forecasts.size, forecasts.map { it.type }.distinct().size)

        val alderForecast = forecasts.first { it.type == PollenType.ALDER }
        assertEquals(listOf(PollenLevel.LOW, PollenLevel.HIGH), alderForecast.levels)
        assertEquals(Trend.RISING, alderForecast.trend)

        val grassForecast = forecasts.first { it.type == PollenType.GRASS }
        assertEquals(listOf(PollenLevel.MEDIUM, PollenLevel.LOW), grassForecast.levels)
        assertEquals(Trend.FALLING, grassForecast.trend)
    }

    @Test
    fun `toForecasts omits types that have level NONE for all days`() {
        val day1 = DailyPollen(
            date = "2023-05-01",
            readings = listOf(
                PollenReading(PollenType.ALDER, PollenLevel.NONE, 0.0),
                PollenReading(PollenType.GRASS, PollenLevel.LOW, 10.0)
            )
        )
        val day2 = DailyPollen(
            date = "2023-05-02",
            readings = listOf(
                PollenReading(PollenType.ALDER, PollenLevel.NONE, 0.0),
                PollenReading(PollenType.GRASS, PollenLevel.NONE, 0.0)
            )
        )

        val forecasts = listOf(day1, day2).toForecasts()

        assertEquals(1, forecasts.size)
        assertEquals(PollenType.GRASS, forecasts.first().type)
        assertEquals(listOf(PollenLevel.LOW, PollenLevel.NONE), forecasts.first().levels)
    }
}
