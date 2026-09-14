package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.DashboardSummary
import java.math.BigDecimal

data class DashboardSummaryDto(
    val totalProjects: Long?,
    val projectsByStatus: Map<String, Long?>?,
    val totalInvestment: BigDecimal?,
    val totalFinancialReturn: BigDecimal?,
    val totalCostReduction: BigDecimal?,
    val estimatedResult: BigDecimal?,
    val averageRoi: BigDecimal?,
    val averageProductivityGain: BigDecimal?
)

fun DashboardSummaryDto.toDomain(): DashboardSummary {
    val mappedTotalProjects = requireNotNull(totalProjects) {
        "O resumo do dashboard não contém o total de projetos."
    }
    require(mappedTotalProjects >= 0) {
        "O total de projetos do dashboard não pode ser negativo."
    }

    val receivedStatusCounts = requireNotNull(projectsByStatus) {
        "O resumo do dashboard não contém os projetos por status."
    }
    val mappedStatusCounts = DashboardSummary.requiredStatuses.associateWith { status ->
        val count = requireNotNull(receivedStatusCounts[status]) {
            "O resumo do dashboard não contém o status $status."
        }
        require(count >= 0) {
            "A quantidade de projetos do status $status não pode ser negativa."
        }
        count
    }
    require(mappedStatusCounts.values.sum() == mappedTotalProjects) {
        "A soma dos projetos por status difere do total do dashboard."
    }

    return DashboardSummary(
        totalProjects = mappedTotalProjects,
        projectsByStatus = mappedStatusCounts,
        totalInvestment = requireMetric(totalInvestment, "investimento total"),
        totalFinancialReturn = requireMetric(
            totalFinancialReturn,
            "retorno financeiro total"
        ),
        totalCostReduction = requireMetric(
            totalCostReduction,
            "redução de custos total"
        ),
        estimatedResult = requireMetric(estimatedResult, "resultado estimado"),
        averageRoi = requireMetric(averageRoi, "ROI médio"),
        averageProductivityGain = requireMetric(
            averageProductivityGain,
            "ganho médio de produtividade"
        )
    )
}

private fun requireMetric(value: BigDecimal?, name: String): BigDecimal {
    return requireNotNull(value) {
        "O resumo do dashboard não contém $name."
    }
}
