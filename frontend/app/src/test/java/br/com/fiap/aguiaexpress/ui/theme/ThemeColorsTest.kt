package br.com.fiap.aguiaexpress.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import br.com.fiap.aguiaexpress.ui.components.statusTone
import br.com.fiap.aguiaexpress.ui.screens.home.HomeSymbol
import br.com.fiap.aguiaexpress.ui.screens.home.tone
import br.com.fiap.aguiaexpress.ui.screens.medalhas.achievementProgress
import org.junit.Assert.*
import org.junit.Test

class ThemeColorsTest {
    @Test
    fun `material schemes preserve supplied palette and layered dark surfaces`() {
        assertEquals(0xFFE84C3D.toInt(), LightColorScheme.primary.toArgb())
        assertEquals(0xFF345C9C.toInt(), LightColorScheme.secondary.toArgb())
        assertEquals(0xFFD9A404.toInt(), LightColorScheme.tertiary.toArgb())
        assertEquals(0xFFFF7B6E.toInt(), DarkColorScheme.primary.toArgb())
        assertEquals(0xFF121316.toInt(), DarkColorScheme.background.toArgb())
        assertEquals(0xFF1A1C20.toInt(), DarkColorScheme.surface.toArgb())
        assertEquals(0xFF252830.toInt(), DarkColorScheme.surfaceContainer.toArgb())
        assertEquals(0xFF3B4048.toInt(), DarkColorScheme.outline.toArgb())
    }

    @Test
    fun `normal text and semantic container foregrounds have accessible contrast in both themes`() {
        listOf(false, true).forEach { dark ->
            val scheme = if (dark) DarkColorScheme else LightColorScheme
            val semantic = semanticColorsFor(dark)
            val pairs = listOf(
                scheme.background to scheme.onBackground, scheme.surface to scheme.onSurface,
                scheme.surfaceContainer to scheme.onSurfaceVariant, scheme.primaryContainer to scheme.onPrimaryContainer,
                scheme.surfaceContainerHighest to scheme.onSurfaceVariant,
                scheme.secondaryContainer to scheme.onSecondaryContainer, scheme.tertiaryContainer to scheme.onTertiaryContainer,
                scheme.errorContainer to scheme.onErrorContainer, semantic.successContainer to semantic.onSuccessContainer,
                semantic.warningContainer to semantic.onWarningContainer, semantic.infoContainer to semantic.onInfoContainer,
                semantic.primaryAction to scheme.onPrimary, scheme.surface to semantic.primaryAction,
                semantic.success to semantic.onSuccess, semantic.warning to semantic.onWarning
            )
            pairs.forEach { (background, content) ->
                assertTrue("Text contrast must be >=4.5:1 (dark=$dark, bg=${background.toArgb()}, fg=${content.toArgb()})",
                    contrast(background, content) >= 4.5f)
            }
        }
    }

    @Test
    fun `operational metric accents distinguish ideas medals votes and approvals`() {
        assertEquals(listOf(SemanticTone.INFO, SemanticTone.ACHIEVEMENT, SemanticTone.PRIMARY, SemanticTone.SUCCESS),
            listOf(HomeSymbol.IDEA, HomeSymbol.BADGE, HomeSymbol.VOTE, HomeSymbol.APPROVED).map { it.tone() })
    }

    @Test
    fun `status tokens share meaning across backend names and formatted labels`() {
        assertEquals(SemanticTone.INFO, statusTone("NOVA"))
        assertEquals(SemanticTone.WARNING, statusTone("EM_ANALISE"))
        assertEquals(statusTone("EM_ANALISE"), statusTone("Em análise"))
        assertEquals(SemanticTone.ACHIEVEMENT, statusTone("PRIORIZADA"))
        listOf("APROVADA", "CONCLUIDO", "CONVERTIDA_EM_PROJETO").forEach { assertEquals(SemanticTone.SUCCESS, statusTone(it)) }
        assertEquals(SemanticTone.ERROR, statusTone("REJEITADA"))
        assertEquals(SemanticTone.NEUTRAL, statusTone("unknown"))
    }

    @Test
    fun `medal progress handles empty catalog and remains within indicator range`() {
        assertEquals(0f, achievementProgress(0, 0), 0f)
        assertEquals(0.5f, achievementProgress(1, 2), 0f)
        assertEquals(1f, achievementProgress(4, 2), 0f)
        assertEquals(0f, achievementProgress(-1, 2), 0f)
    }

    private fun contrast(a: Color, b: Color) = (maxOf(a.luminance(), b.luminance()) + 0.05f) /
        (minOf(a.luminance(), b.luminance()) + 0.05f)
}
