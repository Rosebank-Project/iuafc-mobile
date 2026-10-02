package com.app.iuafc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = LightSecondary,
    onPrimaryContainer = Navy,
    secondary = Gold,
    onSecondary = NavyDeep,
    secondaryContainer = Color(0xFFFBEFD2),
    onSecondaryContainer = NavyDeep,
    tertiary = Gold,
    onTertiary = NavyDeep,
    background = LightBackground,
    onBackground = NavyDeep,
    surface = LightSurface,
    onSurface = NavyDeep,
    surfaceVariant = LightSecondary,
    onSurfaceVariant = LightMutedText,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainerHigh = LightSecondary,
    surfaceContainerHighest = LightSecondary,
    surfaceContainerLowest = LightSurface,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = DarkBackground,
    primaryContainer = DarkSecondary,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSecondary,
    onSecondaryContainer = Gold,
    tertiary = Gold,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSecondary,
    onSurfaceVariant = DarkMutedText,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainerHigh = DarkSecondary,
    surfaceContainerHighest = DarkSecondary,
    surfaceContainerLowest = DarkBackground,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

/**
 * App theme. Uses the church brand colours (navy and gold) instead of the
 * phone's dynamic colours, so the app always matches the website.
 *
 * [darkTheme] will be controlled by the Settings screen (issue #10).
 */
@Composable
fun IUAFCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
