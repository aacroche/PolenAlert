package com.aacroche.polenalert.domain.model

/**
 * Umbrales de la Red Española de Aerobiología (REA), aplicados a la media diaria en granos/m³.
 * Nulo < 1 · Bajo ≤ lowMax · Medio ≤ medMax · Alto > medMax.
 * Ambrosía no figura en la escala REA: se usa la de gramíneas.
 */
enum class PollenType(val lowMax: Double, val medMax: Double) {
    ALDER(30.0, 50.0),
    BIRCH(30.0, 50.0),
    GRASS(25.0, 50.0),
    MUGWORT(25.0, 50.0),
    OLIVE(50.0, 200.0),
    RAGWEED(25.0, 50.0);

    fun levelFor(grainsPerM3: Double): PollenLevel = when {
        grainsPerM3 < 1.0 -> PollenLevel.NONE
        grainsPerM3 <= lowMax -> PollenLevel.LOW
        grainsPerM3 <= medMax -> PollenLevel.MEDIUM
        else -> PollenLevel.HIGH
    }
}