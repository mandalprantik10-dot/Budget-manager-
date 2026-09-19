package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldLight,
    secondary = EmeraldLight,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = CharcoalSurfaceVariant,
    onSecondaryContainer = Color(0xFFE2E8F0),
    error = CoralExpense,
    onError = Color.White,
    errorContainer = CoralContainerDark,
    onErrorContainer = CoralLight,
    background = CharcoalBg,
    onBackground = CharcoalTextPrimary,
    surface = CharcoalSurface,
    onSurface = CharcoalTextPrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = CharcoalTextSecondary,
    outline = CharcoalOutline
)

val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Color(0xFF1E293B),
    error = CoralDark,
    onError = Color.White,
    errorContainer = CoralContainerLight,
    onErrorContainer = Color(0xFF881337),
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
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

