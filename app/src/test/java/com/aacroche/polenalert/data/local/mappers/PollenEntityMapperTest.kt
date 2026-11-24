package com.aacroche.polenalert.data.local.mappers

import com.aacroche.polenalert.data.local.PollenDailyEntity
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PollenEntityMapperTest {

    @Test
    fun `toEntities followed by toDomain returns the same DailyPollen list`() {
        val original = listOf(
            DailyPollen(
                date = "2026-10-14",
                readings = listOf(
                    PollenReading(
                        type = PollenType.GRASS,
                        level = PollenType.GRASS.levelFor(30.0),
                        dailyMean = 30.0
                    ),
                    PollenReading(
                        type = PollenType.OLIVE,
                        level = PollenType.OLIVE.levelFor(150.0),
                        dailyMean = 150.0
                    )
                )
            )
        )

        val result = original.toEntities(java.time.Instant.EPOCH, com.aacroche.polenalert.domain.model.Coordinates(40.42, -3.70)).toDomain()

        assertEquals(original, result)
    }

    @Test
    fun `toSnapshot uses the oldest fetchedAt`() {
        val older = Instant.parse("2026-11-22T08:00:00Z")
        val newer = Instant.parse("2026-11-22T09:00:00Z")
        val rows = listOf(
            PollenDailyEntity(LocalDate.of(2026, 11, 22), PollenType.GRASS, 10.0, newer, latitude = 40.42, longitude = -3.70),
            PollenDailyEntity(LocalDate.of(2026, 11, 22), PollenType.OLIVE, 20.0, older, latitude = 40.42, longitude = -3.70),
        )

        assertEquals(older, rows.toSnapshot().fetchedAt)
    }

    @Test
    fun `toSnapshot of empty list has null fetchedAt`() {
        val snapshot = emptyList<PollenDailyEntity>().toSnapshot()

        assertTrue(snapshot.days.isEmpty())
        assertNull(snapshot.fetchedAt)
    }
}
