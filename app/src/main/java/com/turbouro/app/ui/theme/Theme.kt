package com.turbouro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.turbouro.app.model.AppTheme

@Immutable
data class TurboUroColorScheme(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val isDark: Boolean
)

val DarkColorPalette = TurboUroColorScheme(
    backgroundPrimary = DarkBackgroundPrimary,
    backgroundSecondary = DarkBackgroundSecondary,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    accentPrimary = DarkAccentPrimary,
    accentSecondary = DarkAccentSecondary,
    success = DarkSuccess,
    warning = DarkWarning,
    danger = DarkDanger,
    info = DarkInfo,
    isDark = true
)

val LightColorPalette = TurboUroColorScheme(
    backgroundPrimary = LightBackgroundPrimary,
    backgroundSecondary = LightBackgroundSecondary,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    accentPrimary = LightAccentPrimary,
    accentSecondary = LightAccentSecondary,
    success = LightSuccess,
    warning = LightWarning,
    danger = LightDanger,
    info = LightInfo,
    isDark = false
)

val LocalTurboUroColors = staticCompositionLocalOf { DarkColorPalette }

object TurboUroTheme {
    val colors: TurboUroColorScheme
        @Composable
        get() = LocalTurboUroColors.current
}

@Composable
fun TurboUroAppTheme(
    selectedTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (selectedTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val customColors = if (isDark) DarkColorPalette else LightColorPalette

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = customColors.accentPrimary,
            secondary = customColors.accentSecondary,
            background = customColors.backgroundPrimary,
            surface = customColors.surface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = customColors.accentPrimary,
            secondary = customColors.accentSecondary,
            background = customColors.backgroundPrimary,
            surface = customColors.surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalTurboUroColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = TurboUroTypography,
            shapes = TurboUroShapes,
            content = content
        )
    }
}
