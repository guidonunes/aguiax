package br.com.fiap.aguiax.dto.ai;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiIdeaEvaluationContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldNormalizeCompleteIdeaContextAndPreserveOptionalScores() {
        AiIdeaEvaluationContext context = new AiIdeaEvaluationContext(
                "  Redução de combustível  ",
                "  Reduzir o consumo da frota  ",
                "  Rotas pouco eficientes  ",
                "  Otimizar as rotas  ",
                "  Eficiência operacional  ",
                "  Redução de custos  ",
                0,
                10
        );

        assertAll(
                () -> assertEquals("Redução de combustível", context.title()),
                () -> assertEquals("Reduzir o consumo da frota", context.description()),
                () -> assertEquals("Rotas pouco eficientes", context.problem()),
                () -> assertEquals("Otimizar as rotas", context.proposedSolution()),
                () -> assertEquals("Eficiência operacional", context.category()),
                () -> assertEquals("Redução de custos", context.strategicOrientation()),
                () -> assertEquals(0, context.managerImpactScore()),
                () -> assertEquals(10, context.managerViabilityScore())
        );
    }

    @Test
    void shouldAcceptLegacyIdeaContextWithoutProblemSolutionOrManagerScores() {
        AiIdeaEvaluationContext context = new AiIdeaEvaluationContext(
                "Título",
                "Descrição",
                " ",
                null,
                "Categoria",
                "Orientação",
                null,
                null
        );

        assertAll(
                () -> assertNull(context.problem()),
                () -> assertNull(context.proposedSolution()),
                () -> assertNull(context.managerImpactScore()),
                () -> assertNull(context.managerViabilityScore())
        );
    }

    @Test
    void shouldRejectMissingRequiredIdeaContext() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> contextWithRequiredValues(null, "Descrição", "Categoria", "Orientação")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> contextWithRequiredValues("Título", " ", "Categoria", "Orientação")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> contextWithRequiredValues("Título", "Descrição", "", "Orientação")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> contextWithRequiredValues("Título", "Descrição", "Categoria", null)
                )
        );
    }

    @Test
    void shouldRejectManagerScoresOutsideTheZeroToTenRange() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> contextWithManagerScores(-1, 5)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> contextWithManagerScores(5, 11)
                )
        );
    }

    @Test
    void shouldAcceptAndNormalizeAValidProviderIndependentResult() {
        AiIdeaEvaluationResult result = new AiIdeaEvaluationResult(
                0,
                10,
                8,
                7,
                new BigDecimal("8.10"),
                "  Avaliação estruturada da ideia.  "
        );

        assertAll(
                () -> assertEquals(0, result.strategicAlignment()),
                () -> assertEquals(10, result.impact()),
                () -> assertEquals(8, result.feasibility()),
                () -> assertEquals(7, result.innovation()),
                () -> assertEquals(
                        0,
                        result.overallScore().compareTo(new BigDecimal("8.10"))
                ),
                () -> assertEquals("Avaliação estruturada da ideia.", result.summary())
        );
    }

    @Test
    void shouldRejectMissingOrOutOfRangeProviderResultFields() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(null, 5, 5, 5, new BigDecimal("5.0"), "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, -1, 5, 5, new BigDecimal("5.0"), "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, 5, 11, 5, new BigDecimal("5.0"), "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, 5, 5, 11, new BigDecimal("5.0"), "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, 5, 5, 5, null, "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, 5, 5, 5, new BigDecimal("-0.1"), "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, 5, 5, 5, new BigDecimal("10.1"), "Resumo")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> resultWithScores(5, 5, 5, 5, new BigDecimal("5.0"), " ")
                )
        );
    }

    @Test
    void shouldBuildAndSerializeTheStableApplicationResponse() throws Exception {
        AiIdeaEvaluationResponse response = AiIdeaEvaluationResponse.from(
                42L,
                resultWithScores(
                        9,
                        8,
                        7,
                        8,
                        new BigDecimal("8.1"),
                        "Ideia bem alinhada."
                )
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertAll(
                () -> assertEquals(7, json.size()),
                () -> assertEquals(42, json.get("ideaId").longValue()),
                () -> assertEquals(9, json.get("strategicAlignment").intValue()),
                () -> assertEquals(8, json.get("impact").intValue()),
                () -> assertEquals(7, json.get("feasibility").intValue()),
                () -> assertEquals(8, json.get("innovation").intValue()),
                () -> assertEquals(
                        0,
                        json.get("overallScore")
                                .decimalValue()
                                .compareTo(new BigDecimal("8.1"))
                ),
                () -> assertEquals("Ideia bem alinhada.", json.get("summary").stringValue())
        );
    }

    @Test
    void shouldRejectUntrustedResponseIdentityOrMissingResult() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> AiIdeaEvaluationResponse.from(null, validResult())
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> AiIdeaEvaluationResponse.from(0L, validResult())
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> AiIdeaEvaluationResponse.from(42L, null)
                )
        );
    }

    private AiIdeaEvaluationContext contextWithRequiredValues(
            String title,
            String description,
            String category,
            String strategicOrientation
    ) {
        return new AiIdeaEvaluationContext(
                title,
                description,
                null,
                null,
                category,
                strategicOrientation,
                null,
                null
        );
    }

    private AiIdeaEvaluationContext contextWithManagerScores(
            Integer impact,
            Integer viability
    ) {
        return new AiIdeaEvaluationContext(
                "Título",
                "Descrição",
                "Problema",
                "Solução",
                "Categoria",
                "Orientação",
                impact,
                viability
        );
    }

    private AiIdeaEvaluationResult validResult() {
        return resultWithScores(
                9,
                8,
                7,
                8,
                new BigDecimal("8.1"),
                "Resumo"
        );
    }

    private AiIdeaEvaluationResult resultWithScores(
            Integer strategicAlignment,
            Integer impact,
            Integer feasibility,
            Integer innovation,
            BigDecimal overallScore,
            String summary
    ) {
        return new AiIdeaEvaluationResult(
                strategicAlignment,
                impact,
                feasibility,
                innovation,
                overallScore,
                summary
        );
    }
}
