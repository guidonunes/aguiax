package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.AtualizarAvaliacaoIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.AtualizarStatusIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.AiIdeaEvaluationDto
import br.com.fiap.aguiaexpress.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.IdeiaDto
import br.com.fiap.aguiaexpress.data.remote.api.IdeiaApi
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeiaRepositoryReviewTest {

    @Test
    fun `sends manager scores and maps the persisted response`() = runBlocking {
        val api = RecordingIdeiaApi(response = ideaResponse())
        val repository = IdeiaRepository(api)

        val result = repository.atualizarAvaliacao(
            ideiaId = 42L,
            impacto = 8,
            viabilidade = 7
        )

        assertTrue(result.isSuccess)
        assertEquals(42L, api.receivedIdeaId)
        assertEquals(
            AtualizarAvaliacaoIdeiaRequestDto(8, 7),
            api.receivedRequest
        )
        assertEquals(8, result.getOrThrow().impacto)
        assertEquals(7, result.getOrThrow().viabilidade)
        assertEquals(
            BigDecimal("4.35"),
            result.getOrThrow().prioridade
        )
    }

    @Test
    fun `returns failure when the review request fails`() = runBlocking {
        val expectedFailure = IllegalStateException("Backend unavailable")
        val repository = IdeiaRepository(
            RecordingIdeiaApi(failure = expectedFailure)
        )

        val result = repository.atualizarAvaliacao(
            ideiaId = 42L,
            impacto = 8,
            viabilidade = 7
        )

        assertTrue(result.isFailure)
        assertSame(expectedFailure, result.exceptionOrNull())
    }

    private fun ideaResponse() = IdeiaDto(
        id = 42L,
        usuarioId = 5L,
        categoriaId = 3L,
        orientacaoId = 9L,
        titulo = "Roteirização inteligente",
        descricao = "Otimizar entregas",
        problema = "Rotas ineficientes",
        solucaoProposta = "Usar previsão de demanda",
        impacto = 8,
        viabilidade = 7,
        prioridade = BigDecimal("4.35"),
        statusIdeia = "EM_ANALISE"
    )

    private class RecordingIdeiaApi(
        private val response: IdeiaDto? = null,
        private val failure: RuntimeException? = null
    ) : IdeiaApi {

        var receivedIdeaId: Long? = null
        var receivedRequest: AtualizarAvaliacaoIdeiaRequestDto? = null

        override suspend fun atualizarAvaliacaoIdeia(
            id: Long,
            request: AtualizarAvaliacaoIdeiaRequestDto
        ): IdeiaDto {
            receivedIdeaId = id
            receivedRequest = request
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

        override suspend fun avaliarIdeiaComIa(
            id: Long
        ): AiIdeaEvaluationDto = error("Unexpected call")
    }
}
