package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.DashboardInsightDto
import br.com.fiap.aguiaexpress.data.dto.DashboardSummaryDto
import br.com.fiap.aguiaexpress.data.remote.api.DashboardApi
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

class DashboardRepositoryTest {

    @Test
    fun `declares the backend summary route and orientation query`() {
        val method = DashboardApi::class.java.declaredMethods.first { candidate ->
            candidate.name == "buscarResumo" &&
                candidate.getAnnotation(GET::class.java) != null
        }

        val get = checkNotNull(method.getAnnotation(GET::class.java))
        assertEquals("api/dashboard/summary", get.value)
        val query = method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()
            .single()
        assertEquals("orientationId", query.value)
    }

    @Test
    fun `declares the backend insight route with no request body`() {
        val method = DashboardApi::class.java.declaredMethods.first { candidate ->
            candidate.name == "gerarInsight" &&
                candidate.getAnnotation(POST::class.java) != null
        }

        val post = checkNotNull(method.getAnnotation(POST::class.java))
        assertEquals("api/dashboard/insights", post.value)
        val query = method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()
            .single()
        assertEquals("orientationId", query.value)
    }

    @Test
    fun `loads the global backend summary without an orientation query`() = runBlocking {
        val api = RecordingDashboardApi(response = response())

        val result = DashboardRepository(api).buscarResumo()

        assertTrue(result.isSuccess)
        assertNull(api.receivedOrientationId)
        assertEquals(4L, result.getOrThrow().totalProjects)
    }

    @Test
    fun `forwards the selected strategic orientation to the backend`() = runBlocking {
        val api = RecordingDashboardApi(response = response())

        val result = DashboardRepository(api).buscarResumo(27L)

        assertTrue(result.isSuccess)
        assertEquals(27L, api.receivedOrientationId)
    }

    @Test
    fun `propagates a controlled dashboard request failure`() = runBlocking {
        val expectedFailure = IllegalStateException("Backend unavailable")
        val repository = DashboardRepository(
            RecordingDashboardApi(failure = expectedFailure)
        )

        val result = repository.buscarResumo()

        assertTrue(result.isFailure)
        assertSame(expectedFailure, result.exceptionOrNull())
    }

    @Test
    fun `loads a global insight and preserves its metrics snapshot`() = runBlocking {
        val api = RecordingDashboardApi(
            response = response(),
            insightResponse = insightResponse()
        )

        val result = DashboardRepository(api).gerarInsight()

        assertTrue(result.isSuccess)
        assertNull(api.receivedInsightOrientationId)
        assertEquals(4L, result.getOrThrow().metricsSnapshot.totalProjects)
    }

    @Test
    fun `forwards orientation when generating an insight`() = runBlocking {
        val api = RecordingDashboardApi(
            response = response(),
            insightResponse = insightResponse(
                scope = "ORIENTATION",
                orientationId = 27L
            )
        )

        val result = DashboardRepository(api).gerarInsight(27L)

        assertTrue(result.isSuccess)
        assertEquals(27L, api.receivedInsightOrientationId)
        assertEquals(27L, result.getOrThrow().orientationId)
    }

    @Test
    fun `rejects an insight for a scope different from the request`() = runBlocking {
        val api = RecordingDashboardApi(
            response = response(),
            insightResponse = insightResponse(
                scope = "ORIENTATION",
                orientationId = 99L
            )
        )

        val result = DashboardRepository(api).gerarInsight(27L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    private fun response(): DashboardSummaryDto {
        return DashboardSummaryDto(
            totalProjects = 4,
            projectsByStatus = mapOf(
                "PLANEJADO" to 1,
                "EM_ANDAMENTO" to 2,
                "CONCLUIDO" to 1,
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

    private fun insightResponse(
        scope: String = "GLOBAL",
        orientationId: Long? = null
    ): DashboardInsightDto {
        return DashboardInsightDto(
            scope = scope,
            orientationId = orientationId,
            metricsSnapshot = response(),
            executiveSummary = "O cenário apresenta resultado positivo.",
            highlights = listOf("O retorno supera o investimento."),
            attentionPoints = emptyList(),
            recommendations = listOf("Acompanhar os projetos em andamento."),
            generatedAt = "2026-09-16T15:00:00Z"
        )
    }

    private class RecordingDashboardApi(
        private val response: DashboardSummaryDto? = null,
        private val insightResponse: DashboardInsightDto? = null,
        private val failure: RuntimeException? = null
    ) : DashboardApi {

        var receivedOrientationId: Long? = null
        var receivedInsightOrientationId: Long? = null

        override suspend fun buscarResumo(
            orientationId: Long?
        ): DashboardSummaryDto {
            receivedOrientationId = orientationId
            failure?.let { throw it }
            return checkNotNull(response)
        }

        override suspend fun gerarInsight(
            orientationId: Long?
        ): DashboardInsightDto {
            receivedInsightOrientationId = orientationId
            failure?.let { throw it }
            return checkNotNull(insightResponse)
        }
    }
}
