package br.com.fiap.aguiaexpress.dto.ai;

import java.math.BigDecimal;

public record AiIdeaEvaluationResponse(
        Long ideaId,
        Integer strategicAlignment,
        Integer impact,
        Integer feasibility,
        Integer innovation,
        BigDecimal overallScore,
        String summary
) {

    public AiIdeaEvaluationResponse {
        if (ideaId == null || ideaId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador da ideia deve ser positivo"
            );
        }

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
    }

    public static AiIdeaEvaluationResponse from(
            Long ideaId,
            AiIdeaEvaluationResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "O resultado da avaliação é obrigatório"
            );
        }

        return new AiIdeaEvaluationResponse(
                ideaId,
                result.strategicAlignment(),
                result.impact(),
                result.feasibility(),
                result.innovation(),
                result.overallScore(),
                result.summary()
        );
    }
}
