package com.aacroche.polenalert.ui.theme

import androidx.compose.ui.graphics.Color
import com.aacroche.polenalert.domain.model.PollenLevel


fun PollenLevel.colors(): Pair<Color, Color> = when (this) {   // (contenido, contenedor)
    PollenLevel.NONE -> Gray600 to Background
    PollenLevel.LOW -> LevelLowContent to LevelLowContainer
    PollenLevel.MEDIUM -> LevelMediumContent to LevelMediumContainer
    PollenLevel.HIGH -> LevelHighContent to LevelHighContainer
}
