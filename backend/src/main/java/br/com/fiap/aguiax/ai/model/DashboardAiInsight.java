package br.com.fiap.aguiax.ai.model;

import br.com.fiap.aguiax.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiax.dto.ai.DashboardInsightResult;
import br.com.fiap.aguiax.dto.dashboard.DashboardInsightScope;
import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Document(collection = "dashboard_ai_insights")
public record DashboardAiInsight(
        @Id String id,
        Long requestedByUserId,
        DashboardInsightScope scope,
        Long orientationId,
        String orientationTitle,
        DashboardSummaryResponse metricsSnapshot,
        String language,
        String executiveSummary,
        List<String> highlights,
        List<String> attentionPoints,
        List<String> recommendations,
        String provider,
        String model,
        Instant createdAt
) {

    public DashboardAiInsight {
        id = requireNonBlank(id, "O identificador da análise é obrigatório");
        validatePositiveId(requestedByUserId, "usuário solicitante");

        DashboardInsightContext validatedContext = new DashboardInsightContext(
                scope,
                orientationId,
                orientationTitle,
                metricsSnapshot
        );
        DashboardInsightResult validatedResult = new DashboardInsightResult(
                language,
                executiveSummary,
                highlights,
                attentionPoints,
                recommendations
        );

        scope = validatedContext.scope();
        orientationId = validatedContext.orientationId();
        orientationTitle = validatedContext.orientationTitle();
        metricsSnapshot = validatedContext.metrics();
        language = validatedResult.language();
        executiveSummary = validatedResult.executiveSummary();
        highlights = validatedResult.highlights();
        attentionPoints = validatedResult.attentionPoints();
        recommendations = validatedResult.recommendations();
        provider = requireNonBlank(provider, "O provedor de IA é obrigatório");
        model = requireNonBlank(model, "O modelo de IA é obrigatório");
        createdAt = Objects.requireNonNull(
                createdAt,
                "A data da análise é obrigatória"
        );
    }

    public static DashboardAiInsight from(
            String id,
            Long requestedByUserId,
            DashboardInsightContext context,
            DashboardInsightResult result,
            String provider,
            String model,
            Instant createdAt
    ) {
        Objects.requireNonNull(context, "O contexto da análise é obrigatório");
        Objects.requireNonNull(result, "O resultado da análise é obrigatório");

        return new DashboardAiInsight(
                id,
                requestedByUserId,
                context.scope(),
                context.orientationId(),
                context.orientationTitle(),
                context.metrics(),
                result.language(),
                result.executiveSummary(),
                result.highlights(),
                result.attentionPoints(),
                result.recommendations(),
                provider,
                model,
                createdAt
        );
    }

    private static void validatePositiveId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    "O identificador de " + fieldName + " deve ser positivo"
            );
        }
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
