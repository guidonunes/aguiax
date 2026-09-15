package br.com.fiap.aguiaexpress.ui.theme

import android.graphics.Bitmap
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import br.com.fiap.aguiaexpress.core.settings.ThemeMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.domain.model.*
import br.com.fiap.aguiaexpress.ui.components.PerfilTag
import br.com.fiap.aguiaexpress.ui.components.StatusTag
import br.com.fiap.aguiaexpress.ui.screens.home.HomeContent
import br.com.fiap.aguiaexpress.ui.screens.home.buildHomeOverview
import br.com.fiap.aguiaexpress.ui.screens.ideias.FeedbackMessage
import br.com.fiap.aguiaexpress.ui.screens.medalhas.MedalhasPage
import br.com.fiap.aguiaexpress.viewmodel.BadgeUiState
import br.com.fiap.aguiaexpress.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.math.BigDecimal
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class ThemedScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun operationalHomeDark() { showHome(ApplicationRole.OPERACIONAL); compose.onNodeWithText("Enviar nova ideia").performScrollTo().assertIsDisplayed() }
    @Test fun managerHomeDark() { showHome(ApplicationRole.GESTOR); compose.onNodeWithText("Precisa de atenção").performScrollTo().assertIsDisplayed() }
    @Test fun leaderHomeDark() { showHome(ApplicationRole.LIDER); compose.onNodeWithText("Abrir dashboard completo").performScrollTo().assertIsDisplayed() }
    @Test fun medalsLight() { showMedals(false) }
    @Test fun medalsDark() { showMedals(true) }
    @Test fun statusAndFeedbackLight() { showSemanticComponents(false) }
    @Test fun statusAndFeedbackDark() { showSemanticComponents(true) }

    @Test fun systemThemeReactsToConfigurationWhileExplicitModeOverridesIt() {
        val night = mutableStateOf(false)
        val mode = mutableStateOf(ThemeMode.SYSTEM)
        var background = LightColorScheme.background
        compose.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (night.value) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                AguiaExpressTheme(darkTheme = mode.value.usesDarkTheme(isSystemInDarkTheme())) {
                    background = androidx.compose.material3.MaterialTheme.colorScheme.background
                }
            }
        }
        compose.runOnIdle { assertEquals(LightColorScheme.background, background); night.value = true }
        compose.runOnIdle { assertEquals(DarkColorScheme.background, background); mode.value = ThemeMode.LIGHT }
        compose.runOnIdle { assertEquals(LightColorScheme.background, background); mode.value = ThemeMode.DARK; night.value = false }
        compose.runOnIdle { assertEquals(DarkColorScheme.background, background); mode.value = ThemeMode.SYSTEM }
        compose.runOnIdle { assertEquals(LightColorScheme.background, background) }
    }

    private fun showHome(role: ApplicationRole) {
        val overview = buildHomeOverview(role, 7, inputs())
        compose.setContent { AguiaExpressTheme(darkTheme = true) { HomeContent(HomeUiState("Bom dia, Ana", overview, false), {}, {}) } }
        capture("home-${role.name.lowercase()}-dark")
        compose.onNodeWithText("Bom dia, Ana").assertIsDisplayed()
    }

    private fun showMedals(dark: Boolean) {
        val state = BadgeUiState(totalBadgesAtivas = 2, totalBadgesAtivasConquistadas = 1,
            usuarioBadges = listOf(badge()), badgesDisponiveis = listOf(Badge(2, "IDEIA_APROVADA", "Uma ideia reconhecida pelo impacto.",
                "Tenha uma ideia aprovada.", "", 1)))
        compose.setContent { AguiaExpressTheme(darkTheme = dark) { MedalhasPage(state, {}) } }
        capture("medals-${if (dark) "dark" else "light"}")
        compose.onNodeWithText("Progresso de conquistas").assertIsDisplayed()
        compose.onNodeWithText("Conquistada").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Disponível").performScrollTo().assertIsDisplayed()
    }

    private fun showSemanticComponents(dark: Boolean) {
        compose.setContent { AguiaExpressTheme(darkTheme = dark) { Surface {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("NOVA", "EM_ANALISE", "PRIORIZADA", "APROVADA", "REJEITADA", "CONVERTIDA_EM_PROJETO").forEach { StatusTag(it) }
                PerfilTag("LIDER"); PerfilTag("GESTOR"); PerfilTag("OPERACIONAL")
                FeedbackMessage("Ideia salva com sucesso", false)
                FeedbackMessage("Não foi possível salvar", true)
            }
        } } }
        capture("semantic-${if (dark) "dark" else "light"}")
        compose.onNodeWithText("Ideia salva com sucesso").assertIsDisplayed()
        compose.onNodeWithText("APROVADA").assertIsDisplayed()
    }

    private fun capture(name: String) {
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = (output?.let(::File)
            ?: checkNotNull(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("appearance-previews")))
            .apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun badge() = UsuarioBadge(1, 7, "Ana", "sample@example.test", 1, "PRIMEIRA_IDEIA", "Sua primeira contribuição.",
        "Envie uma ideia.", "", "2026-09-15T12:00:00")

    private fun inputs(): HomeInputs {
        val idea = Ideia(1, "Otimização de rotas", "Descrição", "Problema", "Solução", null, null, BigDecimal.ZERO,
            "EM_ANALISE", 7, 1, 1, "Ana", "Operações", "Eficiência operacional", "2026-09-15T12:00:00", "2026-09-15T12:00:00", 24)
        val project = Projeto(1, 1, idea.titulo, 8, "Gestor", 1, "EM_ANDAMENTO", "Rotas inteligentes", "Descrição", "Objetivo",
            0.0, 0.0, 0.0, 0.0, 0.0, "2026-09-01", "2026-12-01", null, "2026-09-15T12:00:00", "2026-09-15T12:00:00")
        val summary = DashboardSummary(1, mapOf("PLANEJADO" to 0, "EM_ANDAMENTO" to 1, "CONCLUIDO" to 0, "CANCELADO" to 0),
            BigDecimal("120000"), BigDecimal("180000"), BigDecimal("15000"), BigDecimal("75000"), BigDecimal("50"), BigDecimal("12"))
        return HomeInputs(ideas = listOf(idea), badges = listOf(badge()), projects = listOf(project), summary = summary,
            orientations = listOf(Orientacao(1, "Eficiência operacional", "Descrição", "Operações", "Líder", "LIDER",
                "2026-09-15T12:00:00", "2026-09-15T12:00:00")), strategySummaries = mapOf(1L to summary))
    }
}
