package br.com.fiap.aguiaexpress.viewmodel

import br.com.fiap.aguiaexpress.domain.model.DashboardSummary
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardSummaryTest {

    @Test
    fun `uses the backend EM_ANDAMENTO count`() {
        val summary = summaryWithStatusCounts(1, 2, 1, 1)

        assertEquals(2L, summary.projectsInProgress)
    }

    @Test
    fun `returns zero when backend reports no project in progress`() {
        val summary = summaryWithStatusCounts(2, 0, 1, 1)

        assertEquals(0L, summary.projectsInProgress)
    }

    @Test
    fun `does not derive in-progress count from the total`() {
        val summary = summaryWithStatusCounts(5, 1, 3, 2)

        assertEquals(11L, summary.totalProjects)
        assertEquals(1L, summary.projectsInProgress)
    }

    private fun summaryWithStatusCounts(
        planned: Long,
        inProgress: Long,
        completed: Long,
        cancelled: Long
    ): DashboardSummary {
        return DashboardSummary(
            totalProjects = planned + inProgress + completed + cancelled,
            projectsByStatus = mapOf(
                DashboardSummary.STATUS_PLANEJADO to planned,
                DashboardSummary.STATUS_EM_ANDAMENTO to inProgress,
                DashboardSummary.STATUS_CONCLUIDO to completed,
                DashboardSummary.STATUS_CANCELADO to cancelled
            ),
            totalInvestment = BigDecimal.ZERO,
            totalFinancialReturn = BigDecimal.ZERO,
            totalCostReduction = BigDecimal.ZERO,
            estimatedResult = BigDecimal.ZERO,
            averageRoi = BigDecimal.ZERO,
            averageProductivityGain = BigDecimal.ZERO
        )
    }
}
