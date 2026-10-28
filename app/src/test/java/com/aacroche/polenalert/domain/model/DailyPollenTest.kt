package com.aacroche.polenalert.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyPollenTest {

    @Test
    fun `when readings have different levels then highest level is dominant regardless of dailyMean`() {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 30.0, level = PollenLevel.MEDIUM)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 40.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2023-05-01", readings = listOf(oliveReading, grassReading))

        val dominant = dailyPollen.dominantReading()

        assertEquals(PollenType.OLIVE, dominant?.type)
        assertEquals(PollenLevel.MEDIUM, dominant?.level)
    }

    @Test
    fun `when readings have same level then highest dailyMean is dominant`() {
        val mugwortReading = PollenReading(type = PollenType.MUGWORT, dailyMean = 5.0, level = PollenLevel.LOW)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 20.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2023-05-01", readings = listOf(mugwortReading, grassReading))

        val dominant = dailyPollen.dominantReading()

        assertEquals(PollenType.GRASS, dominant?.type)
        assertEquals(20.0, dominant?.dailyMean ?: 0.0, 0.001)
    }

    @Test
    fun `when reading list is empty then dominantReading returns null`() {
        val dailyPollen = DailyPollen(date = "2023-05-01", readings = emptyList())

        val dominant = dailyPollen.dominantReading()

        assertNull(dominant)
    }
}
