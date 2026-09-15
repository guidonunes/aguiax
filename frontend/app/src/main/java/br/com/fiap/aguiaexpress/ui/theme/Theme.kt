package br.com.fiap.aguiaexpress.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

internal val LightColorScheme = lightColorScheme(
    primary = LightPalette.primary, onPrimary = LightPalette.onPrimary,
    primaryContainer = LightPalette.primaryContainer, onPrimaryContainer = LightPalette.onPrimaryContainer,
    secondary = LightPalette.secondary, onSecondary = LightPalette.onPrimary,
    secondaryContainer = LightPalette.secondaryContainer, onSecondaryContainer = LightPalette.onSecondaryContainer,
    tertiary = LightPalette.tertiary, onTertiary = LightPalette.onTertiary,
    tertiaryContainer = LightPalette.tertiaryContainer, onTertiaryContainer = LightPalette.onTertiaryContainer,
    background = LightPalette.background, onBackground = LightPalette.textPrimary,
    surface = LightPalette.surface, onSurface = LightPalette.textPrimary,
    surfaceVariant = LightPalette.surfaceVariant, onSurfaceVariant = LightPalette.textSecondary,
    surfaceDim = LightPalette.surfaceVariant, surfaceBright = LightPalette.surface,
    surfaceContainerLowest = LightPalette.surface, surfaceContainerLow = LightPalette.background,
    surfaceContainer = LightPalette.surfaceVariant, surfaceContainerHigh = LightPalette.surfaceVariant,
    surfaceContainerHighest = LightPalette.surfaceVariant,
    outline = LightPalette.outline, outlineVariant = LightPalette.outline,
    inverseSurface = DarkPalette.surface, inverseOnSurface = DarkPalette.textPrimary,
    inversePrimary = DarkPalette.primary, surfaceTint = LightPalette.primary,
    error = LightPalette.error, onError = LightPalette.onPrimary,
    errorContainer = LightPalette.errorContainer, onErrorContainer = LightPalette.onErrorContainer
)

internal val DarkColorScheme = darkColorScheme(
    primary = DarkPalette.primary, onPrimary = DarkPalette.onPrimary,
    primaryContainer = DarkPalette.primaryContainer, onPrimaryContainer = DarkPalette.onPrimaryContainer,
    secondary = DarkPalette.secondary, onSecondary = DarkPalette.onSecondary,
    secondaryContainer = DarkPalette.secondaryContainer, onSecondaryContainer = DarkPalette.onSecondaryContainer,
    tertiary = DarkPalette.tertiary, onTertiary = DarkPalette.onTertiary,
    tertiaryContainer = DarkPalette.tertiaryContainer, onTertiaryContainer = DarkPalette.onTertiaryContainer,
    background = DarkPalette.background, onBackground = DarkPalette.textPrimary,
    surface = DarkPalette.surface, onSurface = DarkPalette.textPrimary,
    surfaceVariant = DarkPalette.surfaceVariant, onSurfaceVariant = DarkPalette.textSecondary,
    surfaceDim = DarkPalette.background, surfaceBright = DarkPalette.surfaceVariant,
    surfaceContainerLowest = DarkPalette.background, surfaceContainerLow = DarkPalette.surface,
    surfaceContainer = DarkPalette.surfaceVariant, surfaceContainerHigh = DarkPalette.surfaceVariant,
    surfaceContainerHighest = DarkPalette.outline,
    outline = DarkPalette.outline, outlineVariant = DarkPalette.outline,
    inverseSurface = LightPalette.surface, inverseOnSurface = LightPalette.textPrimary,
    inversePrimary = LightPalette.primaryAction, surfaceTint = DarkPalette.primary,
    error = DarkPalette.error, onError = DarkPalette.onError,
    errorContainer = DarkPalette.errorContainer, onErrorContainer = DarkPalette.onErrorContainer
)

@Composable
fun AguiaExpressTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSemanticColors provides semanticColorsFor(darkTheme)) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography, content = content)
    }
}
