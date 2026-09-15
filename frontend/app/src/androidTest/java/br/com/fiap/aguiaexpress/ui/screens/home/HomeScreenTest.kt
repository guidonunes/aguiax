package br.com.fiap.aguiaexpress.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.Instant

class HomeScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun operationalHomeShowsPersonalGreetingAndCreateShortcut() {
        var destination: HomeDestination? = null
        compose.setContent { AguiaExpressTheme { HomeContent(state(ApplicationRole.OPERACIONAL), { destination = it }, {}) } }
        captureLayout("operacional")
        compose.onNodeWithText("Bom dia, Ana").assertIsDisplayed()
        compose.onNodeWithText("SUA CONTRIBUIÇÃO").assertIsDisplayed()
        compose.onNodeWithText("Enviar nova ideia").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(HomeDestination.CREATE_IDEA, destination) }
        compose.onNodeWithText("Abrir dashboard completo").assertDoesNotExist()
        compose.onNodeWithText("Revisar ideias").assertDoesNotExist()
    }

    @Test
    fun managerHomeShowsWorkQueueAndProjectShortcutWithoutCreationOrDashboard() {
        var destination: HomeDestination? = null
        compose.setContent { AguiaExpressTheme { HomeContent(state(ApplicationRole.GESTOR), { destination = it }, {}) } }
        captureLayout("gestor")
        compose.onNodeWithText("GESTÃO DA INOVAÇÃO").assertIsDisplayed()
        compose.onNodeWithText("Precisa de atenção").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Acompanhar projetos").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(HomeDestination.PROJECTS, destination) }
        compose.onNodeWithText("Enviar nova ideia").assertDoesNotExist()
        compose.onNodeWithText("Abrir dashboard completo").assertDoesNotExist()
    }

    @Test
    fun leaderHomeLinksToExistingFullDashboardWithoutOperationalActions() {
        var destination: HomeDestination? = null
        compose.setContent { AguiaExpressTheme { HomeContent(state(ApplicationRole.LIDER), { destination = it }, {}) } }
        captureLayout("lider")
        compose.onNodeWithText("VISÃO EXECUTIVA").assertIsDisplayed()
        compose.onNodeWithText("Abrir dashboard completo").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(HomeDestination.DASHBOARD, destination) }
        compose.onNodeWithText("Enviar nova ideia").assertDoesNotExist()
        compose.onNodeWithText("Revisar ideias").assertDoesNotExist()
    }

    @Test
    fun loadingStateDoesNotInventMetricsAndDisablesRefresh() {
        compose.setContent { AguiaExpressTheme { HomeContent(HomeUiState(greeting = "Bom dia, Ana"), {}, {}) } }
        compose.onNodeWithText("Carregando seu resumo...").assertIsDisplayed()
        compose.onNodeWithContentDescription("Atualizar resumo").assertIsNotEnabled()
        compose.onNodeWithText("Enviar nova ideia").assertDoesNotExist()
    }

    @Test
    fun errorStateOffersRetry() {
        var retries = 0
        compose.setContent { AguiaExpressTheme { HomeContent(HomeUiState(greeting = "Olá", isLoading = false,
            errorMessage = "Não foi possível carregar o início."), {}, { retries++ }) } }
        compose.onNodeWithText("Não foi possível carregar o início.").assertIsDisplayed()
        compose.onNodeWithText("Tentar novamente").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Test
    fun partialDataShowsAvailabilityNoticeAndKeepsValidMetric() {
        val overview = HomeOverview(ApplicationRole.OPERACIONAL,
            listOf(HomeMetric("Ideias criadas", "8", HomeSymbol.IDEA), HomeMetric("Votos recebidos", "—", HomeSymbol.VOTE, "Indisponível")),
            warnings = listOf("Não foi possível carregar suas medalhas."))
        compose.setContent { AguiaExpressTheme { HomeContent(HomeUiState("Bom dia, Ana", overview, false), {}, {}) } }
        compose.onNodeWithText("Resumo parcialmente disponível").assertIsDisplayed()
        compose.onNodeWithText("8").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Indisponível").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun narrowMetricGridUsesSingleColumn() {
        compose.setContent { AguiaExpressTheme { Box(Modifier.width(260.dp)) { HomeMetricGrid(gridMetrics()) } } }
        assertSecondMetricBelowFirst()
    }

    @Test
    fun largeFontMetricGridUsesSingleColumn() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.6f)) {
                AguiaExpressTheme { HomeMetricGrid(gridMetrics()) }
            }
        }
        assertSecondMetricBelowFirst()
    }

    private fun gridMetrics() = listOf(HomeMetric("Ideias", "8", HomeSymbol.IDEA), HomeMetric("Medalhas", "3", HomeSymbol.BADGE))

    private fun assertSecondMetricBelowFirst() {
        val first = compose.onNodeWithText("8").fetchSemanticsNode().boundsInRoot
        val second = compose.onNodeWithText("3").fetchSemanticsNode().boundsInRoot
        assertTrue(second.top >= first.bottom)
    }

    private fun captureLayout(name: String) {
        val outputDirectory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = outputDirectory?.let(::File)
            ?: checkNotNull(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("home-previews"))
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use { stream ->
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    private fun state(role: ApplicationRole): HomeUiState {
        val metrics = when (role) {
            ApplicationRole.OPERACIONAL -> listOf(HomeMetric("Ideias criadas", "8", HomeSymbol.IDEA),
                HomeMetric("Medalhas conquistadas", "3", HomeSymbol.BADGE), HomeMetric("Votos recebidos", "24", HomeSymbol.VOTE),
                HomeMetric("Ideias aprovadas", "2", HomeSymbol.APPROVED))
            ApplicationRole.GESTOR -> listOf(HomeMetric("Aguardando revisão", "6", HomeSymbol.REVIEW),
                HomeMetric("Ideias priorizadas", "3", HomeSymbol.VOTE), HomeMetric("Ideias aprovadas", "2", HomeSymbol.APPROVED),
                HomeMetric("Projetos ativos", "5", HomeSymbol.PROJECT))
            ApplicationRole.LIDER -> listOf(HomeMetric("Projetos em andamento", "4", HomeSymbol.PROJECT),
                HomeMetric("Investimento total", "R$ 120.000,00", HomeSymbol.INVESTMENT),
                HomeMetric("Retorno financeiro", "R$ 180.000,00", HomeSymbol.RETURN), HomeMetric("ROI médio", "50%", HomeSymbol.ROI),
                HomeMetric("Resultado estimado", "R$ 75.000,00", HomeSymbol.RETURN), HomeMetric("Produtividade média", "12%", HomeSymbol.ROI))
        }
        return HomeUiState("Bom dia, Ana", HomeOverview(role, metrics,
            pipeline = listOf(HomeStatusCount("NOVA", 3), HomeStatusCount("EM_ANALISE", 2),
                HomeStatusCount("PRIORIZADA", 1), HomeStatusCount("APROVADA", 2)),
            attention = listOf(HomeAttention("Otimização de rotas", "Priorizada · pronta para decisão", HomeDestination.IDEAS)),
            strategies = listOf(HomeStrategy(1, "Eficiência operacional", 4), HomeStrategy(2, "Sustentabilidade", 2)),
            activities = listOf(HomeActivity("sample", "Ideia · Em análise", "Otimização de rotas",
                Instant.parse("2026-09-15T12:00:00Z"), HomeSymbol.IDEA, HomeDestination.IDEAS))), false)
    }
}
