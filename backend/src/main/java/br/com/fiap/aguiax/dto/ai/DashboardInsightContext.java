package br.com.fiap.aguiax.dto.ai;

import br.com.fiap.aguiax.dto.dashboard.DashboardInsightScope;
import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;

import java.util.Objects;

public record DashboardInsightContext(
        DashboardInsightScope scope,
        Long orientationId,
        String orientationTitle,
        DashboardSummaryResponse metrics
) {

    public DashboardInsightContext {
        scope = Objects.requireNonNull(scope, "O escopo da análise é obrigatório");
        metrics = Objects.requireNonNull(metrics, "As métricas do dashboard são obrigatórias");
        orientationTitle = normalizeOptional(orientationTitle);

        if (metrics.totalProjects() == 0) {
            throw new IllegalArgumentException(
                    "A análise requer ao menos um projeto no dashboard"
            );
        }

        if (scope == DashboardInsightScope.GLOBAL) {
            if (orientationId != null || orientationTitle != null) {
                throw new IllegalArgumentException(
                        "O escopo global não pode conter uma orientação"
                );
            }
        } else if (orientationId == null || orientationId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador da orientação deve ser positivo"
            );
        }
    }

    public static DashboardInsightContext global(
            DashboardSummaryResponse metrics
    ) {
        return new DashboardInsightContext(
                DashboardInsightScope.GLOBAL,
                null,
                null,
                metrics
        );
    }

    public static DashboardInsightContext orientation(
            Long orientationId,
            String orientationTitle,
            DashboardSummaryResponse metrics
    ) {
        return new DashboardInsightContext(
                DashboardInsightScope.ORIENTATION,
                orientationId,
                orientationTitle,
                metrics
        );
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
