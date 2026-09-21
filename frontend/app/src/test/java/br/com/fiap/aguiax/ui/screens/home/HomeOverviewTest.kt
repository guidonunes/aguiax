package br.com.fiap.aguiax.ui.screens.home

import br.com.fiap.aguiax.core.session.ApplicationRole
import br.com.fiap.aguiax.domain.model.HomeInputs
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class HomeOverviewTest {
    @Test
    fun `operational metrics include only own contributions and uncapped received votes`() {
        val inputs = HomeInputs(
            ideas = listOf(homeIdea(1, votes = 20), homeIdea(2, status = "APROVADA", votes = 3),
                homeIdea(3, status = "CONVERTIDA_EM_PROJETO", votes = 1), homeIdea(4, userId = 99, votes = 500)),
            badges = listOf(homeBadge(1), homeBadge(2, badgeId = 1), homeBadge(3, userId = 99)),
            projects = listOf(homeProject(1, ideaId = 3), homeProject(2, ideaId = 4))
        )
        val overview = buildHomeOverview(ApplicationRole.OPERACIONAL, 7, inputs)
        assertEquals(listOf("3", "1", "24", "2"), overview.metrics.map { it.value })
        assertEquals(3, overview.pipeline.sumOf { it.count })
        assertTrue(overview.activities.any { it.id == "project-1" })
        assertFalse(overview.activities.any { it.id in setOf("idea-4", "project-2", "badge-3") })
    }

    @Test
    fun `missing or invalid vote totals do not become zero`() {
        listOf(null, -1).forEach { votes ->
            val overview = buildHomeOverview(ApplicationRole.OPERACIONAL, 7,
                HomeInputs(ideas = listOf(homeIdea(1, votes = votes)), badges = emptyList()))
            assertEquals("—", overview.metrics[2].value)
            assertEquals("Indisponível", overview.metrics[2].hint)
            assertEquals("1", overview.metrics[0].value)
        }
    }

    @Test
    fun `successful empty data produces real zeros and no activity`() {
        val overview = buildHomeOverview(ApplicationRole.OPERACIONAL, 7,
            HomeInputs(ideas = emptyList(), badges = emptyList(), projects = emptyList()))
        assertEquals(listOf("0", "0", "0", "0"), overview.metrics.map { it.value })
        assertTrue(overview.activities.isEmpty())
    }

    @Test
    fun `unavailable sections stay unavailable and keep their warnings`() {
        val warnings = listOf("Não foi possível carregar suas ideias.")
        val overview = buildHomeOverview(ApplicationRole.OPERACIONAL, 7,
            HomeInputs(badges = listOf(homeBadge(1)), warnings = warnings))
        assertEquals(listOf("—", "1", "—", "—"), overview.metrics.map { it.value })
        assertTrue(overview.pipeline.isEmpty())
        assertEquals(warnings, overview.warnings)
    }

    @Test
    fun `manager metrics reflect review queue current approvals and nonterminal projects`() {
        val ideas = listOf(homeIdea(1), homeIdea(2, status = "EM_ANALISE"), homeIdea(3, status = "PRIORIZADA"),
            homeIdea(4, status = "APROVADA"), homeIdea(5, status = "REJEITADA"), homeIdea(6, status = "CONVERTIDA_EM_PROJETO"))
        val projects = listOf(homeProject(1, status = "PLANEJADO"), homeProject(2),
            homeProject(3, status = "CONCLUIDO"), homeProject(4, status = "CANCELADO"))
        val overview = buildHomeOverview(ApplicationRole.GESTOR, 7, HomeInputs(ideas = ideas, projects = projects))
        assertEquals(listOf("2", "1", "1", "2"), overview.metrics.map { it.value })
        assertEquals("Ideia 3", overview.attention.first().title)
        assertEquals(3, overview.attention.size)
        assertTrue(overview.attention.none { it.title in setOf("Ideia 5", "Ideia 6") })
        assertTrue(overview.pipeline.isEmpty())
    }

    @Test
    fun `approved ideas still receive conversion shortcut in manager queue`() {
        val overview = buildHomeOverview(ApplicationRole.GESTOR, 7,
            HomeInputs(ideas = listOf(homeIdea(1, status = "APROVADA")), projects = emptyList()))
        assertTrue(overview.attention.single().detail.contains("conversão"))
        assertEquals(HomeDestination.IDEAS, overview.attention.single().destination)
    }

    @Test
    fun `leader metrics use exact backend summaries rather than project financial fields`() {
        val overview = buildHomeOverview(ApplicationRole.LIDER, 7,
            HomeInputs(summary = homeSummary(), projects = listOf(homeProject(1))))
        val values = overview.metrics.map { it.value.replace('\u00a0', ' ') }
        assertEquals(listOf("3", "R$ 1.234,56", "R$ 2.468,91", "12,34%", "R$ 1.334,60", "5,67%"), values)
        assertTrue(overview.attention.isEmpty())
        assertTrue(overview.activities.isEmpty())
    }

    @Test
    fun `strategic snapshot is bounded alphabetic and distinguishes unavailable counts`() {
        val orientations = listOf(homeOrientation(4, "D"), homeOrientation(3, "C"), homeOrientation(2, "B"), homeOrientation(1, "A"))
        val overview = buildHomeOverview(ApplicationRole.LIDER, 7, HomeInputs(orientations = orientations,
            strategySummaries = mapOf(1L to homeSummary(4), 2L to homeSummary(0))))
        assertEquals(listOf("A", "B", "C"), overview.strategies.map { it.title })
        assertEquals(listOf(4L, 0L, null), overview.strategies.map { it.projectCount })
        assertTrue(overview.metrics.all { it.value == "—" })
    }

    @Test
    fun `recent updates mix real records newest first and never exceed five`() {
        val ideas = (1L..6L).map { homeIdea(it, time = "2026-09-0${it}T12:00:00Z") }
        val overview = buildHomeOverview(ApplicationRole.OPERACIONAL, 7, HomeInputs(ideas = ideas,
            badges = listOf(homeBadge(1)), projects = listOf(homeProject(1))))
        assertEquals(listOf("project-1", "badge-1", "idea-6", "idea-5", "idea-4"), overview.activities.map { it.id })
        assertTrue(overview.activities.none { it.description.contains("novo voto", ignoreCase = true) })
    }

    @Test
    fun `timestamps are not invented and valid creation time is fallback`() {
        val ideas = listOf(homeIdea(1, time = null), homeIdea(2, time = "invalid"),
            homeIdea(3, time = "2026-09-15T12:00:00Z").copy(dataAtualizacao = "invalid"))
        val overview = buildHomeOverview(ApplicationRole.OPERACIONAL, 7, HomeInputs(ideas = ideas))
        assertEquals(listOf("idea-3"), overview.activities.map { it.id })
        assertEquals(Instant.parse("2026-09-15T12:00:00Z"), overview.activities.single().timestamp)
        assertNull(homeTimestamp("invalid"))
        assertEquals(Instant.parse("2026-09-15T15:00:00Z"),
            homeTimestamp("2026-09-15T12:00:00", ZoneId.of("America/Sao_Paulo")))
    }

    @Test
    fun `greeting uses session name and local time without hardcoded fallback user`() {
        assertEquals("Bom dia, Guilherme", homeGreeting("  Guilherme Silva ", 9))
        assertEquals("Boa tarde, Ana", homeGreeting("Ana Maria", 14))
        assertEquals("Boa noite, Ana", homeGreeting("Ana", 20))
        assertEquals("Boa noite", homeGreeting(null, 2))
        assertEquals("Bom dia", homeGreeting("  ", 8))
    }
}
