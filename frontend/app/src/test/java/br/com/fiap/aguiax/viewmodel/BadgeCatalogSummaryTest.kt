package br.com.fiap.aguiax.viewmodel

import br.com.fiap.aguiax.domain.model.Badge
import br.com.fiap.aguiax.domain.model.UsuarioBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeCatalogSummaryTest {

    @Test
    fun `earned badges are excluded from available badges`() {
        val badgesAtivas = listOf(
            badge(1L),
            badge(2L),
            badge(3L)
        )
        val usuarioBadges = listOf(usuarioBadge(2L))

        val resumo = calcularResumoCatalogoBadges(
            badgesAtivas = badgesAtivas,
            usuarioBadges = usuarioBadges
        )

        assertEquals(listOf(1L, 3L), resumo.badgesDisponiveis.map { it.id })
        assertTrue(
            resumo.badgesDisponiveis.none { badge ->
                usuarioBadges.any { usuarioBadge ->
                    usuarioBadge.badgeId == badge.id
                }
            }
        )
        assertEquals(3, resumo.totalBadgesAtivas)
        assertEquals(1, resumo.totalBadgesAtivasConquistadas)
    }

    @Test
    fun `all earned active badges leave no available badges`() {
        val badgesAtivas = listOf(badge(1L), badge(2L))
        val usuarioBadges = listOf(usuarioBadge(1L), usuarioBadge(2L))

        val resumo = calcularResumoCatalogoBadges(
            badgesAtivas = badgesAtivas,
            usuarioBadges = usuarioBadges
        )

        assertTrue(resumo.badgesDisponiveis.isEmpty())
        assertEquals(2, resumo.totalBadgesAtivas)
        assertEquals(2, resumo.totalBadgesAtivasConquistadas)
    }

    @Test
    fun `inactive historical awards do not inflate active badge progress`() {
        val badgesAtivas = listOf(badge(1L), badge(2L))
        val usuarioBadges = listOf(
            usuarioBadge(2L),
            usuarioBadge(99L)
        )

        val resumo = calcularResumoCatalogoBadges(
            badgesAtivas = badgesAtivas,
            usuarioBadges = usuarioBadges
        )

        assertEquals(listOf(1L), resumo.badgesDisponiveis.map { it.id })
        assertEquals(2, resumo.totalBadgesAtivas)
        assertEquals(1, resumo.totalBadgesAtivasConquistadas)
    }

    @Test
    fun `all active badges remain available when user has no awards`() {
        val badgesAtivas = listOf(badge(1L), badge(2L))

        val resumo = calcularResumoCatalogoBadges(
            badgesAtivas = badgesAtivas,
            usuarioBadges = emptyList()
        )

        assertEquals(listOf(1L, 2L), resumo.badgesDisponiveis.map { it.id })
        assertEquals(2, resumo.totalBadgesAtivas)
        assertEquals(0, resumo.totalBadgesAtivasConquistadas)
    }

    private fun badge(id: Long): Badge {
        return Badge(
            id = id,
            nome = "Badge $id",
            descricao = "Descrição",
            criterio = "Critério",
            icone = "ICONE",
            status = 1
        )
    }

    private fun usuarioBadge(badgeId: Long): UsuarioBadge {
        return UsuarioBadge(
            id = badgeId,
            usuarioId = 7L,
            nomeUsuario = "Usuário",
            emailUsuario = "usuario@example.com",
            badgeId = badgeId,
            nomeBadge = "Badge $badgeId",
            descricaoBadge = "Descrição",
            criterioBadge = "Critério",
            iconeBadge = "ICONE",
            dataConquista = "2026-09-10"
        )
    }
}
