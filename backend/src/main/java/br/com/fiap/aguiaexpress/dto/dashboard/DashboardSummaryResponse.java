package br.com.fiap.aguiaexpress.dto.dashboard;

import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record DashboardSummaryResponse(
        long totalProjects,
        Map<NomeStatusProjeto, Long> projectsByStatus,
        BigDecimal totalInvestment,
        BigDecimal totalFinancialReturn,
        BigDecimal totalCostReduction,
        BigDecimal estimatedResult,
        BigDecimal averageRoi,
        BigDecimal averageProductivityGain
) {

    public DashboardSummaryResponse {
        if (totalProjects < 0) {
            throw new IllegalArgumentException("O total de projetos não pode ser negativo");
        }

        Objects.requireNonNull(projectsByStatus, "A contagem de projetos por status é obrigatória");
        totalInvestment = Objects.requireNonNull(totalInvestment, "O investimento total é obrigatório");
        totalFinancialReturn = Objects.requireNonNull(
                totalFinancialReturn,
                "O retorno financeiro total é obrigatório"
        );
        totalCostReduction = Objects.requireNonNull(
                totalCostReduction,
                "A redução de custos total é obrigatória"
        );
        estimatedResult = Objects.requireNonNull(estimatedResult, "O resultado estimado é obrigatório");
        averageRoi = Objects.requireNonNull(averageRoi, "O ROI médio é obrigatório");
        averageProductivityGain = Objects.requireNonNull(
                averageProductivityGain,
                "O ganho médio de produtividade é obrigatório"
        );

        EnumMap<NomeStatusProjeto, Long> completeStatusCounts =
                new EnumMap<>(NomeStatusProjeto.class);
        for (NomeStatusProjeto status : NomeStatusProjeto.values()) {
            completeStatusCounts.put(status, 0L);
        }

        projectsByStatus.forEach((status, count) -> {
            Objects.requireNonNull(status, "O status do projeto é obrigatório");
            Objects.requireNonNull(count, "A contagem de projetos por status é obrigatória");
            if (count < 0) {
                throw new IllegalArgumentException("A contagem de projetos por status não pode ser negativa");
            }
            completeStatusCounts.put(status, count);
        });

        long projectsCountedByStatus = completeStatusCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        if (projectsCountedByStatus != totalProjects) {
            throw new IllegalArgumentException(
                    "A soma dos projetos por status deve ser igual ao total de projetos"
            );
        }

        projectsByStatus = Collections.unmodifiableMap(completeStatusCounts);
    }
}
