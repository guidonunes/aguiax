package br.com.fiap.aguiaexpress.viewmodel

import br.com.fiap.aguiaexpress.domain.model.Projeto
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardSummaryTest {

    @Test
    fun `counts only projects with EM_ANDAMENTO status`() {
        val projetos = listOf(
            project(1L, "PLANEJADO"),
            project(2L, "EM_ANDAMENTO"),
            project(3L, "CONCLUIDO"),
            project(4L, "EM_ANDAMENTO"),
            project(5L, "CANCELADO")
        )

        assertEquals(2, contarProjetosEmAndamento(projetos))
    }

    @Test
    fun `returns zero when no project is in progress`() {
        val projetos = listOf(
            project(1L, "PLANEJADO"),
            project(2L, "CONCLUIDO"),
            project(3L, "CANCELADO")
        )

        assertEquals(0, contarProjetosEmAndamento(projetos))
    }

    @Test
    fun `counts every project when all are in progress`() {
        val projetos = listOf(
            project(1L, "EM_ANDAMENTO"),
            project(2L, "EM_ANDAMENTO"),
            project(3L, "EM_ANDAMENTO")
        )

        assertEquals(3, contarProjetosEmAndamento(projetos))
    }

    private fun project(id: Long, status: String): Projeto {
        return Projeto(
            id = id,
            ideiaId = id,
            tituloIdeia = "Ideia $id",
            usuarioId = 7L,
            nomeUsuarioResponsavel = "Gestor",
            statusProjetoId = 1L,
            statusProjeto = status,
            titulo = "Projeto $id",
            descricao = "Descrição",
            objetivo = "Objetivo",
            investimento = 0.0,
            roi = 0.0,
            retornoFinanceiro = 0.0,
            reducaoCustos = 0.0,
            ganhoProdutividade = 0.0,
            dataInicio = "2026-09-10",
            dataPrevisaoFim = "2026-12-10",
            dataConclusao = null
        )
    }
}
