package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.IdeiaDto
import br.com.fiap.aguiaexpress.data.remote.api.IdeiaApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import java.math.BigDecimal

class IdeiaRepositoryPersonalListTest {
    @Test
    fun `personal list forwards authenticated user id and maps home data`() = runBlocking {
        var requestedId: Long? = null
        val api = api { method, args ->
            assertEquals("listarIdeiasPorUsuario", method)
            requestedId = args?.first() as Long
            listOf(IdeiaDto(id = 42, usuarioId = 7, categoriaId = 1, orientacaoId = 1,
                titulo = "Ideia", descricao = "Descrição", problema = "Problema", solucaoProposta = "Solução",
                impacto = null, viabilidade = null, prioridade = BigDecimal.ZERO, statusIdeia = "NOVA",
                totalVotos = 24, dataAtualizacao = "2026-09-15T12:00:00"))
        }
        val ideas = IdeiaRepository(api).listarIdeiasPorUsuario(7).getOrThrow()
        assertEquals(7L, requestedId)
        assertEquals(24, ideas.single().totalVotos)
        assertEquals("2026-09-15T12:00:00", ideas.single().dataAtualizacao)
    }

    @Test
    fun `personal list returns failure for API error`() = runBlocking {
        val failure = IllegalStateException("unavailable")
        val result = IdeiaRepository(api { _, _ -> throw failure }).listarIdeiasPorUsuario(7)
        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `personal list never swallows cancellation`() {
        assertThrows(CancellationException::class.java) {
            runBlocking { IdeiaRepository(api { _, _ -> throw CancellationException() }).listarIdeiasPorUsuario(7) }
        }
    }

    private fun api(handler: (String, Array<out Any?>?) -> Any?): IdeiaApi = Proxy.newProxyInstance(
        IdeiaApi::class.java.classLoader, arrayOf(IdeiaApi::class.java)
    ) { _, method, args -> handler(method.name, args) } as IdeiaApi
}
