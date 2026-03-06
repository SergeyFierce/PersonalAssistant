package ru.topskiy.personalassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

@Immutable
data class WarmExtendedColors(
    val surfaceElevated: androidx.compose.ui.graphics.Color,
    val divider: androidx.compose.ui.graphics.Color,
    val disabled: androidx.compose.ui.graphics.Color,
    val primaryPressed: androidx.compose.ui.graphics.Color
)

private val LightExtendedColors = WarmExtendedColors(
    surfaceElevated = WarmLightSurfaceElevated,
    divider = WarmLightDivider,
    disabled = WarmLightDisabled,
    primaryPressed = WarmLightPrimaryPressed
)

private val DarkExtendedColors = WarmExtendedColors(
    surfaceElevated = WarmDarkSurfaceElevated,
    divider = WarmDarkDivider,
    disabled = WarmDarkDisabled,
    primaryPressed = WarmDarkPrimaryPressed
)

val LocalWarmExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object WarmTheme {
    val extendedColors: WarmExtendedColors
        @Composable
        get() = LocalWarmExtendedColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = WarmDarkPrimary,
    onPrimary = WarmDarkOnPrimary,
    primaryContainer = WarmDarkPrimaryContainer,
    secondary = WarmDarkSecondary,
    onSecondary = WarmDarkOnSecondary,
    background = WarmDarkBackground,
    onBackground = WarmDarkOnSurface,
    surface = WarmDarkSurface,
    onSurface = WarmDarkOnSurface,
    surfaceVariant = WarmDarkSurfaceVariant,
    onSurfaceVariant = WarmDarkOnSurfaceVariant,
    outline = WarmDarkOutline,
    error = WarmDarkError,
    onError = WarmDarkOnError
)

private val LightColorScheme = lightColorScheme(
    primary = WarmLightPrimary,
    onPrimary = WarmLightOnPrimary,
    primaryContainer = WarmLightPrimaryContainer,
    secondary = WarmLightSecondary,
    onSecondary = WarmLightOnSecondary,
    background = WarmLightBackground,
    onBackground = WarmLightOnSurface,
    surface = WarmLightSurface,
    onSurface = WarmLightOnSurface,
    surfaceVariant = WarmLightSurfaceVariant,
    onSurfaceVariant = WarmLightOnSurfaceVariant,
    outline = WarmLightOutline,
    error = WarmLightError,
    onError = WarmLightOnError
)

@Composable
fun PersonalAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalWarmExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}