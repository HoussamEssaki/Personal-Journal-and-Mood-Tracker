package com.personaljournal.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = SecondaryGreen,
    background = DarkSurface,
    surface = DarkCard,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = SecondaryGreen,
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun PersonalJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = PrimaryBlue,
    fontScale: Float = 1f,
    highContrast: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val accent = accentColor
    val contrastSurface = if (darkTheme) Color(0xFF06090F) else Color.White
    val contrastOnSurface = if (darkTheme) Color.White else Color.Black
    val onPrimaryColor = if (highContrast && !darkTheme) Color.Black else Color.White
    val colors = baseScheme.copy(
        primary = accent,
        secondary = accent,
        onPrimary = onPrimaryColor,
        surface = if (highContrast) contrastSurface else baseScheme.surface,
        background = if (highContrast) contrastSurface else baseScheme.background,
        onSurface = if (highContrast) contrastOnSurface else baseScheme.onSurface
    )

    val currentDensity = LocalDensity.current
    val scaledDensity = remember(currentDensity, fontScale) {
        Density(currentDensity.density, fontScale.coerceIn(0.9f, 1.4f))
    }
    val motionScale = if (reduceMotion) 0f else 1f

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalMotionScale provides motionScale
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}

val LocalMotionScale = staticCompositionLocalOf { 1f }
