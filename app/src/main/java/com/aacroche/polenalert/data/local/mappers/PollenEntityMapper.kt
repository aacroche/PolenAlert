package com.aacroche.polenalert.data.local.mappers

import com.aacroche.polenalert.data.local.PollenDailyEntity
import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenSnapshot
import java.time.Instant
import java.time.LocalDate

fun List<DailyPollen>.toEntities(fetchedAt: Instant, coordinates: Coordinates): List<PollenDailyEntity> =
    flatMap { day ->
        day.readings.map {
            PollenDailyEntity(
                LocalDate.parse(day.date),
                it.type,
                it.dailyMean,
                fetchedAt,
                coordinates.latitude,
                coordinates.longitude
            )
        }
    }

fun List<PollenDailyEntity>.toDomain(): List<DailyPollen> =
    groupBy { it.date }.map { (date, rows) ->
        DailyPollen(
            date = date.toString(),
            readings = rows.map {
                PollenReading(
                    it.type,
                    it.type.levelFor(it.dailyMean),
                    it.dailyMean,
                )
            },
        )
    }

fun List<PollenDailyEntity>.toSnapshot(): PollenSnapshot =
    PollenSnapshot(days = toDomain(), fetchedAt = minOfOrNull { it.fetchedAt })
