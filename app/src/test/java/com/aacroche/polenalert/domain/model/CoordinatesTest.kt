package com.aacroche.polenalert.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinatesTest {
    private val madrid = Coordinates(40.4167, -3.7033)
    private val sevilla = Coordinates(37.3891, -5.9845)

    @Test
    fun `distanceKmTo to mismo same point is zero`{
        val madrid = Coordinates(40.4168, -3.7038)

        val distance = madrid.distanceKmTo(madrid)

        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `distanceKmTo from Madrid to Seville is 390 km`() {
        val madrid = Coordinates(40.4168, -3.7038)
        val sevilla = Coordinates(37.3891, -5.9845)

        val distance = madrid.distanceKmTo(sevilla)

        assertEquals(390.0, distance, 10.0)
    }

    @Test
    fun `distance to the same point is zero`() {
        assertEquals(0.0, madrid.distanceKmTo(madrid), 0.001)
    }

    @Test
    fun `distance from Madrid to Sevilla is about 390 km`() {
        assertEquals(390.0, madrid.distanceKmTo(sevilla), 5.0)
    }

    @Test
    fun `distance is symmetric`() {
        assertEquals(madrid.distanceKmTo(sevilla), sevilla.distanceKmTo(madrid), 0.001)
    }

    @Test
    fun `isInPollenCoverage only for Europe`() {
        assertTrue(madrid.isInPollenCoverage())
        assertFalse(Coordinates(-34.6037, -58.3816).isInPollenCoverage())   // Buenos Aires
    }
}
