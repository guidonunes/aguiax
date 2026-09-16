package br.com.fiap.aguiaexpress.viewmodel

import br.com.fiap.aguiaexpress.data.dto.CriarOrientacaoRequestDto
import br.com.fiap.aguiaexpress.data.dto.DashboardInsightDto
import br.com.fiap.aguiaexpress.data.dto.DashboardSummaryDto
import br.com.fiap.aguiaexpress.data.dto.EditarOrientacaoRequestDto
import br.com.fiap.aguiaexpress.data.dto.OrientacaoDto
import br.com.fiap.aguiaexpress.data.remote.api.DashboardApi
import br.com.fiap.aguiaexpress.data.remote.api.OrientacaoApi
import br.com.fiap.aguiaexpress.data.repository.DashboardRepository
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.test.MainDispatcherRule
import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardInsightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps dashboard visible while generating and adopts returned snapshot`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val api = FakeDashboardApi().apply {
                summaries[null] = summaryDto(totalProjects = 2)
                insightResponses[null] = CompletableDeferred()
            }
            val viewModel = viewModel(api)
            advanceUntilIdle()

            viewModel.gerarInsight()
            runCurrent()

            assertTrue(viewModel.insightUiState.value.isLoading)
            assertEquals(2L, viewModel.uiState.value.summary?.totalProjects)

            api.insightResponses.getValue(null).complete(
                insightDto(totalProjects = 4)
            )
            advanceUntilIdle()

            assertFalse(viewModel.insightUiState.value.isLoading)
            assertEquals(
                "O portfólio apresenta resultado positivo.",
                viewModel.insightUiState.value.insight?.executiveSummary
            )
            assertEquals(4L, viewModel.uiState.value.summary?.totalProjects)
            assertEquals(
                viewModel.insightUiState.value.insight?.metricsSnapshot,
                viewModel.uiState.value.summary
            )
        }

    @Test
    fun `ignores a late insight response after orientation changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val api = FakeDashboardApi().apply {
                summaries[null] = summaryDto(totalProjects = 2)
                summaries[42L] = summaryDto(totalProjects = 3)
                insightResponses[null] = CompletableDeferred()
            }
            val viewModel = viewModel(api)
            advanceUntilIdle()

            viewModel.gerarInsight()
            runCurrent()
            viewModel.selecionarOrientacao(42L)
            advanceUntilIdle()

            assertEquals(42L, viewModel.uiState.value.orientacaoSelecionadaId)
            assertEquals(3L, viewModel.uiState.value.summary?.totalProjects)
            assertNull(viewModel.insightUiState.value.insight)

            api.insightResponses.getValue(null).complete(
                insightDto(totalProjects = 9)
            )
            advanceUntilIdle()

            assertEquals(3L, viewModel.uiState.value.summary?.totalProjects)
            assertNull(viewModel.insightUiState.value.insight)
            assertFalse(viewModel.insightUiState.value.isLoading)
        }

    @Test
    fun `clears a completed insight immediately when orientation changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val api = FakeDashboardApi().apply {
                summaries[null] = summaryDto(totalProjects = 2)
                summaries[42L] = summaryDto(totalProjects = 3)
                insightResponses[null] = CompletableDeferred(
                    insightDto(totalProjects = 2)
                )
            }
            val viewModel = viewModel(api)
            advanceUntilIdle()
            viewModel.gerarInsight()
            advanceUntilIdle()
            assertTrue(viewModel.insightUiState.value.insight != null)

            viewModel.selecionarOrientacao(42L)

            assertNull(viewModel.insightUiState.value.insight)
            assertNull(viewModel.insightUiState.value.errorMessage)
        }

    @Test
    fun `forwards the currently selected orientation to insight endpoint`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val api = FakeDashboardApi().apply {
                summaries[null] = summaryDto(totalProjects = 2)
                summaries[42L] = summaryDto(totalProjects = 3)
                insightResponses[42L] = CompletableDeferred(
                    insightDto(
                        scope = "ORIENTATION",
                        orientationId = 42L,
                        totalProjects = 3
                    )
                )
            }
            val viewModel = viewModel(api)
            advanceUntilIdle()
            viewModel.selecionarOrientacao(42L)
            advanceUntilIdle()

            viewModel.gerarInsight()
            advanceUntilIdle()

            assertEquals(listOf(42L), api.insightRequests)
            assertEquals(42L, viewModel.insightUiState.value.insight?.orientationId)
        }

    @Test
    fun `role policy prevents non-leader insight request`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val api = FakeDashboardApi().apply {
                summaries[null] = summaryDto(totalProjects = 2)
            }
            val viewModel = viewModel(api, canRequestInsight = false)
            advanceUntilIdle()

            viewModel.gerarInsight()

            assertEquals(
                "A análise executiva é exclusiva para líderes.",
                viewModel.insightUiState.value.errorMessage
            )
            assertTrue(api.insightRequests.isEmpty())
        }

    @Test
    fun `failed insight keeps summary visible and can be retried`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val firstRequest = CompletableDeferred<DashboardInsightDto>()
            val api = FakeDashboardApi().apply {
                summaries[null] = summaryDto(totalProjects = 2)
                insightResponses[null] = firstRequest
            }
            val viewModel = viewModel(api)
            advanceUntilIdle()

            viewModel.gerarInsight()
            runCurrent()
            firstRequest.completeExceptionally(
                IllegalStateException("provider unavailable")
            )
            advanceUntilIdle()

            assertEquals(2L, viewModel.uiState.value.summary?.totalProjects)
            assertEquals(
                "Não foi possível gerar a análise executiva.",
                viewModel.insightUiState.value.errorMessage
            )

            api.insightResponses[null] = CompletableDeferred(
                insightDto(totalProjects = 4)
            )
            viewModel.gerarInsight()
            advanceUntilIdle()

            assertNull(viewModel.insightUiState.value.errorMessage)
            assertEquals(4L, viewModel.uiState.value.summary?.totalProjects)
            assertEquals(2, api.insightRequests.size)
        }

    private fun viewModel(
        api: DashboardApi,
        canRequestInsight: Boolean = true
    ): DashboardViewModel {
        return DashboardViewModel(
            dashboardRepository = DashboardRepository(api),
            orientacaoRepository = OrientacaoRepository(
                FakeOrientacaoApi(listOf(orientationDto()))
            ),
            canRequestInsight = { canRequestInsight }
        )
    }

    private fun summaryDto(totalProjects: Long): DashboardSummaryDto {
        return DashboardSummaryDto(
            totalProjects = totalProjects,
            projectsByStatus = mapOf(
                "PLANEJADO" to 1,
                "EM_ANDAMENTO" to totalProjects - 1,
                "CONCLUIDO" to 0,
                "CANCELADO" to 0
            ),
            totalInvestment = BigDecimal("100.00"),
            totalFinancialReturn = BigDecimal("200.00"),
            totalCostReduction = BigDecimal("25.00"),
            estimatedResult = BigDecimal("125.00"),
            averageRoi = BigDecimal("10.50"),
            averageProductivityGain = BigDecimal("5.25")
        )
    }

    private fun insightDto(
        scope: String = "GLOBAL",
        orientationId: Long? = null,
        totalProjects: Long
    ): DashboardInsightDto {
        return DashboardInsightDto(
            scope = scope,
            orientationId = orientationId,
            metricsSnapshot = summaryDto(totalProjects),
            executiveSummary = "O portfólio apresenta resultado positivo.",
            highlights = listOf("O retorno supera o investimento."),
            attentionPoints = emptyList(),
            recommendations = listOf("Acompanhar os projetos em andamento."),
            generatedAt = "2026-09-16T15:00:00Z"
        )
    }

    private fun orientationDto(): OrientacaoDto {
        return OrientacaoDto(
            id = 42L,
            titulo = "Eficiência operacional",
            descricao = "Reduzir custos",
            areaEstrategica = "Operações",
            status = 1,
            usuarioId = 17L,
            nomeUsuario = "Líder",
            perfilUsuario = "LIDER",
            dataCriacao = "2026-09-01T10:00:00",
            dataAtualizacao = "2026-09-01T10:00:00"
        )
    }

    private class FakeDashboardApi : DashboardApi {
        val summaries = mutableMapOf<Long?, DashboardSummaryDto>()
        val insightResponses =
            mutableMapOf<Long?, CompletableDeferred<DashboardInsightDto>>()
        val insightRequests = mutableListOf<Long?>()

        override suspend fun buscarResumo(
            orientationId: Long?
        ): DashboardSummaryDto {
            return summaries.getValue(orientationId)
        }

        override suspend fun gerarInsight(
            orientationId: Long?
        ): DashboardInsightDto {
            insightRequests += orientationId
            return insightResponses.getValue(orientationId).await()
        }
    }

    private class FakeOrientacaoApi(
        private val orientations: List<OrientacaoDto>
    ) : OrientacaoApi {

        override suspend fun listarOrientacoes(): List<OrientacaoDto> {
            return orientations
        }

        override suspend fun listarOrientacoesAtivas(): List<OrientacaoDto> {
            return orientations
        }

        override suspend fun criarOrientacao(
            request: CriarOrientacaoRequestDto
        ): OrientacaoDto = error("Operação não utilizada neste teste")

        override suspend fun editarOrientacao(
            id: Long,
            request: EditarOrientacaoRequestDto
        ): OrientacaoDto = error("Operação não utilizada neste teste")

        override suspend fun buscarOrientacaoPorId(id: Long): OrientacaoDto =
            error("Operação não utilizada neste teste")

        override suspend fun deletarOrientacao(id: Long, usuarioId: Long) {
            error("Operação não utilizada neste teste")
        }
    }
}
