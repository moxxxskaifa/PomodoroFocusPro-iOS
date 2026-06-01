package com.pomodorofocus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PomodoroPrimary,
    onPrimary = Color.White,
    primaryContainer = PomodoroPrimaryLight,
    secondary = PomodoroSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3E0),
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0EDE8),
    onSurfaceVariant = TextSecondary,
    outline = DividerLight,
    error = PomodoroPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = PomodoroPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1520),
    secondary = PomodoroSecondary,
    onSecondary = Color(0xFF3D2200),
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = CardBackgroundDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E1E32),
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    error = Color(0xFFCF6679),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
