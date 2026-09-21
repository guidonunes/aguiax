package br.com.fiap.aguiax.dto.ai;

import java.math.BigDecimal;

public record AiIdeaEvaluationResult(
        Integer strategicAlignment,
        Integer impact,
        Integer feasibility,
        Integer innovation,
        BigDecimal overallScore,
        String summary
) {

    private static final BigDecimal MINIMUM_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAXIMUM_SCORE = BigDecimal.TEN;

    public AiIdeaEvaluationResult {
        validateIntegerScore(strategicAlignment, "strategicAlignment");
        validateIntegerScore(impact, "impact");
        validateIntegerScore(feasibility, "feasibility");
        validateIntegerScore(innovation, "innovation");
        validateDecimalScore(overallScore, "overallScore");

        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException(
                    "O resumo da avaliação de IA é obrigatório"
            );
        }

        summary = summary.trim();
    }

    private static void validateIntegerScore(Integer score, String fieldName) {
        if (score == null) {
            throw new IllegalArgumentException(
                    "A pontuação " + fieldName + " é obrigatória"
            );
        }

        if (score < 0 || score > 10) {
            throw new IllegalArgumentException(
                    "A pontuação " + fieldName + " deve estar entre 0 e 10"
            );
        }
    }

    private static void validateDecimalScore(
            BigDecimal score,
            String fieldName
    ) {
        if (score == null) {
            throw new IllegalArgumentException(
                    "A pontuação " + fieldName + " é obrigatória"
            );
        }

        if (score.compareTo(MINIMUM_SCORE) < 0
                || score.compareTo(MAXIMUM_SCORE) > 0) {
            throw new IllegalArgumentException(
                    "A pontuação " + fieldName + " deve estar entre 0 e 10"
            );
        }
    }
}
