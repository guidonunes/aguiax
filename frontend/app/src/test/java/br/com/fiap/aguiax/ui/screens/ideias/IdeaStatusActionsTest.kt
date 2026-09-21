package br.com.fiap.aguiax.ui.screens.ideias

import br.com.fiap.aguiax.domain.model.StatusIdeia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeaStatusActionsTest {
    @Test fun `new ideas must start analysis before a decision`() {
        assertEquals(listOf(StatusIdeia.EM_ANALISE), statusActionsForIdea("NOVA"))
    }
    @Test fun `ideas under analysis can be prioritized or approved`() {
        assertEquals(listOf(StatusIdeia.PRIORIZADA, StatusIdeia.APROVADA), statusActionsForIdea("EM_ANALISE"))
    }
    @Test fun `prioritized ideas cannot be prioritized again`() {
        assertEquals(listOf(StatusIdeia.APROVADA), statusActionsForIdea("PRIORIZADA"))
    }
    @Test fun `terminal and unknown states expose no decision actions`() {
        listOf("APROVADA", "REJEITADA", "CONVERTIDA_EM_PROJETO", "UNKNOWN", "")
            .forEach { assertTrue(statusActionsForIdea(it).isEmpty()) }
    }
}
