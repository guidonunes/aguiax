package br.com.fiap.aguiaexpress.ai.model;

import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResult;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "idea_ai_evaluations")
public record AiIdeaEvaluation(
        @Id String id,
        Long ideaId,
        Long requestedByUserId,
        String provider,
        String model,
        Integer strategicAlignment,
        Integer impact,
        Integer feasibility,
        Integer innovation,
        BigDecimal overallScore,
        String summary,
        Instant createdAt
) {

    public AiIdeaEvaluation {
        id = requireNonBlank(id, "O identificador da avaliação é obrigatório");
        validatePositiveId(ideaId, "ideia");
        validatePositiveId(requestedByUserId, "usuário solicitante");
        provider = requireNonBlank(provider, "O provedor de IA é obrigatório");
        model = requireNonBlank(model, "O modelo de IA é obrigatório");

        AiIdeaEvaluationResult validatedResult = new AiIdeaEvaluationResult(
                strategicAlignment,
                impact,
                feasibility,
                innovation,
                overallScore,
                summary
        );

        strategicAlignment = validatedResult.strategicAlignment();
        impact = validatedResult.impact();
        feasibility = validatedResult.feasibility();
        innovation = validatedResult.innovation();
        overallScore = validatedResult.overallScore();
        summary = validatedResult.summary();

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "A data da avaliação é obrigatória"
            );
        }
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
