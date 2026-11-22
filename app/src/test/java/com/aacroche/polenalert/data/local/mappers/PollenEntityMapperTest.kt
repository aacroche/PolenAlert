package com.aacroche.polenalert.data.local.mappers

import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import org.junit.Assert.assertEquals
import org.junit.Test

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

        val result = original.toEntities(java.time.Instant.EPOCH).toDomain()

        assertEquals(original, result)
    }
}
