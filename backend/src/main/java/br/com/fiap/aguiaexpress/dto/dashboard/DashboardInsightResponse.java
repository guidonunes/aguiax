package br.com.fiap.aguiaexpress.dto.dashboard;

import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Schema(description = "Análise executiva consultiva das métricas do dashboard")
public record DashboardInsightResponse(
        DashboardInsightScope scope,
        Long orientationId,
        DashboardSummaryResponse metricsSnapshot,
        @Schema(maxLength = 800)
        String executiveSummary,
        List<String> highlights,
        List<String> attentionPoints,
        List<String> recommendations,
        Instant generatedAt
) {

    public DashboardInsightResponse {
        DashboardInsightContext validatedContext = new DashboardInsightContext(
                scope,
                orientationId,
                null,
                metricsSnapshot
        );
        DashboardInsightResult validatedResult = new DashboardInsightResult(
                DashboardInsightResult.REQUIRED_LANGUAGE,
                executiveSummary,
                highlights,
                attentionPoints,
                recommendations
        );

        scope = validatedContext.scope();
        orientationId = validatedContext.orientationId();
        metricsSnapshot = validatedContext.metrics();
        executiveSummary = validatedResult.executiveSummary();
        highlights = validatedResult.highlights();
        attentionPoints = validatedResult.attentionPoints();
        recommendations = validatedResult.recommendations();
        generatedAt = Objects.requireNonNull(
                generatedAt,
                "A data de geração da análise é obrigatória"
        );
    }

    public static DashboardInsightResponse from(
            DashboardInsightContext context,
            DashboardInsightResult result,
            Instant generatedAt
    ) {
        Objects.requireNonNull(context, "O contexto da análise é obrigatório");
        Objects.requireNonNull(result, "O resultado da análise é obrigatório");

        return new DashboardInsightResponse(
                context.scope(),
                context.orientationId(),
                context.metrics(),
                result.executiveSummary(),
                result.highlights(),
                result.attentionPoints(),
                result.recommendations(),
                generatedAt
        );
    }
}
