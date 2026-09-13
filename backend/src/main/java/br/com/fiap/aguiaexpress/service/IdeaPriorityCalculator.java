package br.com.fiap.aguiaexpress.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class IdeaPriorityCalculator {

    private static final BigDecimal VOTE_WEIGHT =
            new BigDecimal("0.20");
    private static final BigDecimal IMPACT_WEIGHT =
            new BigDecimal("0.25");
    private static final BigDecimal VIABILITY_WEIGHT =
            new BigDecimal("0.20");
    private static final BigDecimal AI_WEIGHT =
            new BigDecimal("0.35");

    private static final BigDecimal MINIMUM_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAXIMUM_SCORE = BigDecimal.TEN;

    public BigDecimal calculate(
            Integer totalVotes,
            Integer impactScore,
            Integer viabilityScore,
            BigDecimal aiScore
    ) {
        BigDecimal normalizedVotes = normalizeVotes(totalVotes);
        BigDecimal impact = normalizeOptionalIntegerScore(
                impactScore,
                "impactScore"
        );
        BigDecimal viability = normalizeOptionalIntegerScore(
                viabilityScore,
                "viabilityScore"
        );
        BigDecimal ai = normalizeOptionalDecimalScore(aiScore, "aiScore");

        return normalizedVotes.multiply(VOTE_WEIGHT)
                .add(impact.multiply(IMPACT_WEIGHT))
                .add(viability.multiply(VIABILITY_WEIGHT))
                .add(ai.multiply(AI_WEIGHT))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeVotes(Integer totalVotes) {
        int votes = totalVotes == null ? 0 : totalVotes;
        int normalized = Math.min(Math.max(votes, 0), 10);
        return BigDecimal.valueOf(normalized);
    }

    private BigDecimal normalizeOptionalIntegerScore(
            Integer score,
            String fieldName
    ) {
        int normalized = score == null ? 0 : score;

        if (normalized < 0 || normalized > 10) {
            throw new IllegalArgumentException(
                    fieldName + " deve estar entre 0 e 10"
            );
        }

        return BigDecimal.valueOf(normalized);
    }

    private BigDecimal normalizeOptionalDecimalScore(
            BigDecimal score,
            String fieldName
    ) {
        BigDecimal normalized = score == null ? BigDecimal.ZERO : score;

        if (normalized.compareTo(MINIMUM_SCORE) < 0
                || normalized.compareTo(MAXIMUM_SCORE) > 0) {
            throw new IllegalArgumentException(
                    fieldName + " deve estar entre 0 e 10"
            );
        }

        return normalized;
    }
}
