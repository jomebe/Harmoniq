package com.jomebe.harmoniq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HarmoniqColors = darkColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    secondary = Cyan,
    tertiary = Rose,
    background = Ink,
    onBackground = TextPrimary,
    surface = InkRaised,
    onSurface = TextPrimary,
    surfaceVariant = InkSoft,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF303446)
)

@Composable
fun HarmoniqTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HarmoniqColors,
        typography = Typography(),
        content = content
    )
}
