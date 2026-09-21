package br.com.fiap.aguiax.dto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record DashboardInsightResult(
        String language,
        String executiveSummary,
        List<String> highlights,
        List<String> attentionPoints,
        List<String> recommendations
) {

    public static final String REQUIRED_LANGUAGE = "pt-BR";
    public static final int MAXIMUM_SUMMARY_LENGTH = 800;
    public static final int MAXIMUM_ITEM_LENGTH = 300;

    public DashboardInsightResult {
        if (!REQUIRED_LANGUAGE.equals(language)) {
            throw new IllegalArgumentException(
                    "O idioma da análise deve ser pt-BR"
            );
        }

        executiveSummary = normalizeRequiredText(
                executiveSummary,
                "O resumo executivo é obrigatório",
                MAXIMUM_SUMMARY_LENGTH,
                "O resumo executivo deve ter no máximo 800 caracteres"
        );
        highlights = normalizeItems(
                highlights,
                1,
                3,
                "destaques"
        );
        attentionPoints = normalizeItems(
                attentionPoints,
                0,
                3,
                "pontos de atenção"
        );
        recommendations = normalizeItems(
                recommendations,
                1,
                3,
                "sugestões de melhoria"
        );
    }

    private static List<String> normalizeItems(
            List<String> values,
            int minimumSize,
            int maximumSize,
            String fieldName
    ) {
        if (values == null) {
            throw new IllegalArgumentException(
                    "A lista de " + fieldName + " é obrigatória"
            );
        }

        if (values.size() < minimumSize || values.size() > maximumSize) {
            throw new IllegalArgumentException(
                    "A lista de " + fieldName + " deve conter entre "
                            + minimumSize + " e " + maximumSize + " itens"
            );
        }

        List<String> normalized = new ArrayList<>(values.size());
        for (String value : values) {
            normalized.add(normalizeRequiredText(
                    value,
                    "Os itens de " + fieldName + " não podem ser vazios",
                    MAXIMUM_ITEM_LENGTH,
                    "Cada item de " + fieldName
                            + " deve ter no máximo 300 caracteres"
            ));
        }

        return Collections.unmodifiableList(normalized);
    }

    private static String normalizeRequiredText(
            String value,
            String requiredMessage,
            int maximumLength,
            String lengthMessage
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(requiredMessage);
        }

        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(lengthMessage);
        }

        return normalized;
    }
}
