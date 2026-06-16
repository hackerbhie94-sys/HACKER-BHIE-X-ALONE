package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberGreen,
    onPrimary = Color.Black,
    primaryContainer = MatrixDimGreen,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    tertiary = CyberGreenDark,
    background = DeepBlack,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = CyberGreen,
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    tertiary = LightPrimary,
    background = LightBackground,
    onBackground = Color.Black,
    surface = LightSurface,
    onSurface = Color.Black,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
