package com.aacroche.polenalert.domain.model

data class DailyPollen(val date: String, val readings: List<PollenReading>)

fun DailyPollen.dominantReading(): PollenReading? =
    readings.maxWithOrNull(
        compareBy<PollenReading>
        { it.level }.thenBy { it.dailyMean }
    )