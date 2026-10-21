package com.aacroche.polenalert.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.aacroche.polenalert.R
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenType


fun PollenLevel.colors(): Pair<Color, Color> = when (this) {   // (contenido, contenedor)
    PollenLevel.NONE -> Gray600 to Background
    PollenLevel.LOW -> LevelLowContent to LevelLowContainer
    PollenLevel.MEDIUM -> LevelMediumContent to LevelMediumContainer
    PollenLevel.HIGH -> LevelHighContent to LevelHighContainer
}

fun PollenLevel.labelRes(): Int = when (this) {
    PollenLevel.NONE -> R.string.pollen_level_none
    PollenLevel.LOW -> R.string.pollen_level_low
    PollenLevel.MEDIUM -> R.string.pollen_level_medium
    PollenLevel.HIGH -> R.string.pollen_level_high
}

@Composable
fun PollenType.nameRes() : String = when(this) {
    PollenType.ALDER -> stringResource(R.string.pollen_type_alder)
    PollenType.BIRCH -> stringResource(R.string.pollen_type_birch)
    PollenType.GRASS -> stringResource(R.string.pollen_type_grass)
    PollenType.OLIVE -> stringResource(R.string.pollen_type_olive)
    PollenType.MUGWORT -> stringResource(R.string.pollen_type_mugwort)
    PollenType.RAGWEED -> stringResource(R.string.pollen_type_ragweed)
}

@Composable
fun PollenType.scientificName() : String = when(this) {
    PollenType.ALDER -> "Alnus"
    PollenType.BIRCH -> "Betula"
    PollenType.GRASS -> "Poaceae"
    PollenType.OLIVE -> "Olea europaea"
    PollenType.MUGWORT -> "Artemisia"
    PollenType.RAGWEED -> "Ambrosia"
}

@Composable
fun PollenType.getImg() : Int = when(this) {
    PollenType.ALDER -> R.drawable.img_alder
    PollenType.BIRCH -> R.drawable.img_birch
    PollenType.GRASS -> R.drawable.img_gramineas
    PollenType.OLIVE -> R.drawable.img_olivo
    PollenType.MUGWORT -> R.drawable.img_mugwort
    PollenType.RAGWEED -> R.drawable.img_ragweed
}