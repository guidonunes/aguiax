package br.com.fiap.aguiaexpress.dto.ai;

public record AiIdeaEvaluationContext(
        String title,
        String description,
        String problem,
        String proposedSolution,
        String category,
        String strategicOrientation,
        Integer managerImpactScore,
        Integer managerViabilityScore
) {

    public AiIdeaEvaluationContext {
        title = requireNonBlank(title, "O título da ideia é obrigatório");
        description = requireNonBlank(
                description,
                "A descrição da ideia é obrigatória"
        );
        category = requireNonBlank(category, "A categoria da ideia é obrigatória");
        strategicOrientation = requireNonBlank(
                strategicOrientation,
                "A orientação estratégica é obrigatória"
        );

        problem = normalizeOptional(problem);
        proposedSolution = normalizeOptional(proposedSolution);

        validateOptionalScore(managerImpactScore, "impacto do gestor");
        validateOptionalScore(managerViabilityScore, "viabilidade do gestor");
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static void validateOptionalScore(Integer score, String fieldName) {
        if (score != null && (score < 0 || score > 10)) {
            throw new IllegalArgumentException(
                    "A pontuação de " + fieldName + " deve estar entre 0 e 10"
            );
        }
    }
}
