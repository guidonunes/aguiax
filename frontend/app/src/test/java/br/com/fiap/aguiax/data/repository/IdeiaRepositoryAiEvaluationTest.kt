package br.com.fiap.aguiax.data.repository

import br.com.fiap.aguiax.data.dto.AiIdeaEvaluationDto
import br.com.fiap.aguiax.data.dto.AtualizarAvaliacaoIdeiaRequestDto
import br.com.fiap.aguiax.data.dto.AtualizarStatusIdeiaRequestDto
import br.com.fiap.aguiax.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiax.data.dto.IdeiaDto
import br.com.fiap.aguiax.data.remote.api.IdeiaApi
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeiaRepositoryAiEvaluationTest {

    @Test
    fun `requests AI evaluation for the selected idea and maps its response`() = runBlocking {
        val api = RecordingAiIdeiaApi(response = evaluationResponse(42L))
        val repository = IdeiaRepository(api)

        val result = repository.avaliarIdeiaComIa(42L)

        assertTrue(result.isSuccess)
        assertEquals(42L, api.receivedIdeaId)
        assertEquals(42L, result.getOrThrow().ideaId)
        assertEquals(BigDecimal("8.1"), result.getOrThrow().overallScore)
        assertEquals("Resumo validado.", result.getOrThrow().summary)
    }

    @Test
    fun `rejects a response for a different idea`() = runBlocking {
        val repository = IdeiaRepository(
            RecordingAiIdeiaApi(response = evaluationResponse(99L))
        )

        val result = repository.avaliarIdeiaComIa(42L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `returns failure when AI request fails`() = runBlocking {
        val expectedFailure = IllegalStateException("Backend unavailable")
        val repository = IdeiaRepository(
            RecordingAiIdeiaApi(failure = expectedFailure)
        )

        val result = repository.avaliarIdeiaComIa(42L)

        assertTrue(result.isFailure)
        assertSame(expectedFailure, result.exceptionOrNull())
    }

    private fun evaluationResponse(ideaId: Long) = AiIdeaEvaluationDto(
        ideaId = ideaId,
        strategicAlignment = 9,
        impact = 8,
        feasibility = 7,
        innovation = 8,
        overallScore = BigDecimal("8.1"),
        summary = "Resumo validado."
    )

    private class RecordingAiIdeiaApi(
        private val response: AiIdeaEvaluationDto? = null,
        private val failure: RuntimeException? = null
    ) : IdeiaApi {

        var receivedIdeaId: Long? = null

        override suspend fun avaliarIdeiaComIa(id: Long): AiIdeaEvaluationDto {
            receivedIdeaId = id
            failure?.let { throw it }
            return checkNotNull(response)
        }

        override suspend fun listarIdeias(): List<IdeiaDto> =
            error("Unexpected call")

        override suspend fun listarIdeiasPorUsuario(usuarioId: Long): List<IdeiaDto> =
            error("Unexpected call")

        override suspend fun buscarIdeiaPorId(id: Long): IdeiaDto =
            error("Unexpected call")

        override suspend fun criarIdeia(
            request: CriarIdeiaRequestDto
        ): IdeiaDto = error("Unexpected call")

        override suspend fun atualizarStatusIdeia(
            id: Long,
            request: AtualizarStatusIdeiaRequestDto
        ): IdeiaDto = error("Unexpected call")

        override suspend fun deletarIdeia(id: Long) {
            error("Unexpected call")
        }

        override suspend fun editarIdeia(
            id: Long,
            request: CriarIdeiaRequestDto
        ): IdeiaDto = error("Unexpected call")

        override suspend fun atualizarAvaliacaoIdeia(
            id: Long,
            request: AtualizarAvaliacaoIdeiaRequestDto
        ): IdeiaDto = error("Unexpected call")
    }
}
