package com.aacroche.polenalert.data.local.mappers

import com.aacroche.polenalert.data.local.PollenDailyEntity
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.PollenReading
import java.time.LocalDate

fun List<DailyPollen>.toEntities(): List<PollenDailyEntity> =
    flatMap { day ->
        day.readings.map {
            PollenDailyEntity(
                LocalDate.parse(day.date),
                it.type,
                it.dailyMean
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