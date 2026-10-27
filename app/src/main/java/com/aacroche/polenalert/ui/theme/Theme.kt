package com.aacroche.polenalert.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Green800,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondaryContainer = Green250,
    secondary = Green700,
    background = Background,
    onBackground = Green900,
    surface = Surface,
    onSurface = Green900,
    onSurfaceVariant = Gray600,
    outline = Gray500,
    outlineVariant = Green200,
)

@Composable
fun PolenAlertTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}