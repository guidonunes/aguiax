package br.com.fiap.aguiaexpress.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SemanticColors(
    val success: Color, val onSuccess: Color, val successContainer: Color, val onSuccessContainer: Color,
    val warning: Color, val onWarning: Color, val warningContainer: Color, val onWarningContainer: Color,
    val info: Color, val infoContainer: Color, val onInfoContainer: Color,
    // The light brand coral/white pair is <4.5:1. Small text/filled buttons use this darker brand tone.
    val primaryAction: Color
)

internal fun semanticColorsFor(dark: Boolean): SemanticColors = if (dark) SemanticColors(
    DarkPalette.success, DarkPalette.onSuccess, DarkPalette.successContainer, DarkPalette.onSuccessContainer,
    DarkPalette.warning, DarkPalette.onWarning, DarkPalette.warningContainer, DarkPalette.onWarningContainer,
    DarkPalette.secondary, DarkPalette.secondaryContainer, DarkPalette.onSecondaryContainer, DarkPalette.primary
) else SemanticColors(
    LightPalette.success, LightPalette.onPrimary, LightPalette.successContainer, LightPalette.onSuccessContainer,
    LightPalette.warning, LightPalette.textPrimary, LightPalette.warningContainer, LightPalette.onWarningContainer,
    LightPalette.secondary, LightPalette.secondaryContainer, LightPalette.onSecondaryContainer, LightPalette.primaryAction
)

internal val LocalSemanticColors = staticCompositionLocalOf { semanticColorsFor(false) }
val MaterialTheme.semanticColors: SemanticColors
    @Composable @ReadOnlyComposable get() = LocalSemanticColors.current

internal enum class SemanticTone { INFO, WARNING, ACHIEVEMENT, SUCCESS, ERROR, NEUTRAL, PRIMARY }
internal data class ToneColors(val container: Color, val content: Color)

@Composable
internal fun toneColors(tone: SemanticTone): ToneColors {
    val scheme = MaterialTheme.colorScheme
    val semantic = MaterialTheme.semanticColors
    return when (tone) {
        SemanticTone.INFO -> ToneColors(semantic.infoContainer, semantic.onInfoContainer)
        SemanticTone.WARNING -> ToneColors(semantic.warningContainer, semantic.onWarningContainer)
        SemanticTone.ACHIEVEMENT -> ToneColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
        SemanticTone.SUCCESS -> ToneColors(semantic.successContainer, semantic.onSuccessContainer)
        SemanticTone.ERROR -> ToneColors(scheme.errorContainer, scheme.onErrorContainer)
        SemanticTone.NEUTRAL -> ToneColors(scheme.surfaceContainer, scheme.onSurfaceVariant)
        SemanticTone.PRIMARY -> ToneColors(scheme.primaryContainer, scheme.onPrimaryContainer)
    }
}
