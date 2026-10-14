package com.aacroche.polenalert.domain.model

enum class PollenType(val lowMax: Double, val medMax: Double) {
    ALDER(30.0, 50.0),
    BIRCH(30.0, 80.0),
    GRASS(15.0, 30.0),
    MUGWORT(10.0, 30.0),
    OLIVE(50.0, 100.0),
    RAGWEED(10.0, 30.0);

    fun levelFor(grainsPerM3: Double): PollenLevel = when {
        grainsPerM3 <= 0.5 -> PollenLevel.NONE
        grainsPerM3 <= lowMax -> PollenLevel.LOW
        grainsPerM3 <= medMax -> PollenLevel.MEDIUM
        grainsPerM3 > medMax -> PollenLevel.HIGH
        else -> PollenLevel.HIGH
    }
}