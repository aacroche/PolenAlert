package com.aacroche.polenalert.domain.model

import com.aacroche.polenalert.domain.model.Trend.FALLING
import com.aacroche.polenalert.domain.model.Trend.RISING
import com.aacroche.polenalert.domain.model.Trend.STABLE

enum class Trend {
    RISING, STABLE, FALLING;
}

fun List<PollenLevel>.toTrend() : Trend = when {
    this.isEmpty() -> STABLE
    this.first() < this.last() -> RISING
    this.first() > this.last() -> FALLING
    else -> STABLE
}

data class PollenForecast(
    val type: PollenType,
    val levels: List<PollenLevel>,   // uno por día, en orden
    val trend: Trend,
)

fun List<DailyPollen>.toForecasts(): List<PollenForecast> {
    val allTypes = flatMap { it.readings }.map { it.type }.distinct()
    return allTypes.mapNotNull { type ->
        val levels = map { dailyPollen ->
            dailyPollen.readings.firstOrNull { it.type == type }?.level ?: PollenLevel.NONE
        }
        if (levels.all { it == PollenLevel.NONE }) {
            null
        } else {
            PollenForecast(
                type = type,
                levels = levels,
                trend = levels.toTrend()
            )
        }
    }
}