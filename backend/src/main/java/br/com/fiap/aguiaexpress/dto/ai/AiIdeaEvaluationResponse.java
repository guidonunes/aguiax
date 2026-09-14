package br.com.fiap.aguiaexpress.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Avaliação consultiva estruturada produzida pela IA")
public record AiIdeaEvaluationResponse(
        Long ideaId,
        @Schema(minimum = "0", maximum = "10", example = "9")
        Integer strategicAlignment,
        @Schema(minimum = "0", maximum = "10", example = "8")
        Integer impact,
        @Schema(minimum = "0", maximum = "10", example = "7")
        Integer feasibility,
        @Schema(minimum = "0", maximum = "10", example = "8")
        Integer innovation,
        @Schema(minimum = "0", maximum = "10", example = "8.10")
        BigDecimal overallScore,
        @Schema(example = "A ideia possui bom alinhamento e impacto esperado.")
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
