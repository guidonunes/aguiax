package br.com.fiap.aguiaexpress.viewmodel

import br.com.fiap.aguiaexpress.domain.model.Ideia
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class IdeaFeedOrderTest {
    @Test
    fun `newest creation is first regardless of id or priority`() {
        val older = idea(99, "2026-09-14T12:00:00").copy(prioridade = BigDecimal.TEN)
        val newer = idea(1, "2026-09-15T12:00:00")

        assertEquals(listOf(newer, older), ordenarIdeiasMaisRecentes(listOf(older, newer)))
    }

    @Test
    fun `equal creation times use descending ids for stable ordering`() {
        val time = "2026-09-15T12:00:00.123456"
        assertEquals(
            listOf(3L, 2L, 1L),
            ordenarIdeiasMaisRecentes(listOf(idea(1, time), idea(3, time), idea(2, time)))
                .map { it.id }
        )
    }

    @Test
    fun `legacy missing or invalid dates do not break the feed or mutate input`() {
        val input = listOf(idea(1, null), idea(3, "invalid"), idea(2, "2026-09-15T12:00:00"))
        assertEquals(listOf(2L, 3L, 1L), ordenarIdeiasMaisRecentes(input).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), input.map { it.id })
    }

    @Test
    fun `empty feed stays empty`() {
        assertEquals(emptyList<Ideia>(), ordenarIdeiasMaisRecentes(emptyList()))
    }

    private fun idea(id: Long, createdAt: String?) = Ideia(
        id = id,
        titulo = "Ideia $id",
        descricao = "Descrição",
        problema = "Problema",
        solucaoProposta = "Solução",
        impacto = null,
        viabilidade = null,
        prioridade = BigDecimal.ZERO,
        status = "NOVA",
        usuarioId = 1,
        categoriaId = 1,
        orientacaoId = 1,
        nomeUsuario = "Usuário",
        nomeCategoria = "Categoria",
        tituloOrientacao = "Orientação",
        dataCriacao = createdAt
    )
}
