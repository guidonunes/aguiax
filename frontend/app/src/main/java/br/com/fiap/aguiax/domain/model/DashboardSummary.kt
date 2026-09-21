package br.com.fiap.aguiax.domain.model

import java.math.BigDecimal

data class DashboardSummary(
    val totalProjects: Long,
    val projectsByStatus: Map<String, Long>,
    val totalInvestment: BigDecimal,
    val totalFinancialReturn: BigDecimal,
    val totalCostReduction: BigDecimal,
    val estimatedResult: BigDecimal,
    val averageRoi: BigDecimal,
    val averageProductivityGain: BigDecimal
) {
    val projectsInProgress: Long
        get() = projectsByStatus.getValue(STATUS_EM_ANDAMENTO)

    companion object {
        const val STATUS_PLANEJADO = "PLANEJADO"
        const val STATUS_EM_ANDAMENTO = "EM_ANDAMENTO"
        const val STATUS_CONCLUIDO = "CONCLUIDO"
        const val STATUS_CANCELADO = "CANCELADO"

        val requiredStatuses = setOf(
            STATUS_PLANEJADO,
            STATUS_EM_ANDAMENTO,
            STATUS_CONCLUIDO,
            STATUS_CANCELADO
        )
    }
}
