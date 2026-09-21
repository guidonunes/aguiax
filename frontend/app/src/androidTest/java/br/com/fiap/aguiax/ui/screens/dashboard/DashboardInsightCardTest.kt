package br.com.fiap.aguiax.ui.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import br.com.fiap.aguiax.domain.model.DashboardInsight
import br.com.fiap.aguiax.domain.model.DashboardInsightScope
import br.com.fiap.aguiax.domain.model.DashboardSummary
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import br.com.fiap.aguiax.viewmodel.DashboardInsightUiState
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardInsightCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun initialStateExplainsTheFeatureAndRequiresAnExplicitAction() {
        var clicks = 0
        composeRule.setContent {
            AguiaXTheme {
                DashboardInsightCard(
                    state = DashboardInsightUiState(),
                    onGenerate = { clicks++ }
                )
            }
        }

        composeRule.onNodeWithText("Análise executiva com IA")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Gerar análise")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun loadingStateKeepsAVisiblePortugueseProgressMessageAndDisablesAction() {
        composeRule.setContent {
            AguiaXTheme {
                DashboardInsightCard(
                    state = DashboardInsightUiState(isLoading = true),
                    onGenerate = {}
                )
            }
        }

        composeRule.onNodeWithText("Gerando análise executiva...")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Gerando análise...")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun successStateDisplaysEveryStructuredSectionWithoutChatControls() {
        composeRule.setContent {
            AguiaXTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    DashboardInsightCard(
                        state = DashboardInsightUiState(insight = insight()),
                        onGenerate = {}
                    )
                }
            }
        }

        listOf(
            "Resumo executivo",
            "Destaques",
            "Pontos de atenção",
            "Sugestões de melhoria",
            "O cenário apresenta resultado positivo.",
            "• O retorno supera o investimento.",
            "Nenhum ponto de atenção foi indicado para este recorte.",
            "• Acompanhar os projetos em andamento.",
            "Gerar novamente"
        ).forEach { text ->
            composeRule.onNodeWithText(text)
                .performScrollTo()
                .assertIsDisplayed()
        }

        composeRule.onNodeWithText("Enviar mensagem").assertDoesNotExist()
    }

    private fun insight(): DashboardInsight {
        return DashboardInsight(
            scope = DashboardInsightScope.GLOBAL,
            orientationId = null,
            metricsSnapshot = DashboardSummary(
                totalProjects = 2,
                projectsByStatus = mapOf(
                    DashboardSummary.STATUS_PLANEJADO to 1,
                    DashboardSummary.STATUS_EM_ANDAMENTO to 1,
                    DashboardSummary.STATUS_CONCLUIDO to 0,
                    DashboardSummary.STATUS_CANCELADO to 0
                ),
                totalInvestment = BigDecimal("100.00"),
                totalFinancialReturn = BigDecimal("200.00"),
                totalCostReduction = BigDecimal("25.00"),
                estimatedResult = BigDecimal("125.00"),
                averageRoi = BigDecimal("10.50"),
                averageProductivityGain = BigDecimal("5.25")
            ),
            executiveSummary = "O cenário apresenta resultado positivo.",
            highlights = listOf("O retorno supera o investimento."),
            attentionPoints = emptyList(),
            recommendations = listOf("Acompanhar os projetos em andamento."),
            generatedAt = Instant.parse("2026-09-16T15:00:00Z")
        )
    }
}
