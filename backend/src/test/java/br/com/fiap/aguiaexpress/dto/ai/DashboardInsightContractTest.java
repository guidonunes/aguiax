package br.com.fiap.aguiaexpress.dto.ai;

import br.com.fiap.aguiaexpress.dto.dashboard.DashboardInsightResponse;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardInsightScope;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardInsightContractTest {

    private static final Instant GENERATED_AT =
            Instant.parse("2026-09-16T15:00:00Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildTrustedGlobalAndOrientationContexts() {
        DashboardSummaryResponse metrics = metrics(2);

        DashboardInsightContext global = DashboardInsightContext.global(metrics);
        DashboardInsightContext orientation = DashboardInsightContext.orientation(
                7L,
                "  Eficiência operacional  ",
                metrics
        );

        assertAll(
                () -> assertEquals(DashboardInsightScope.GLOBAL, global.scope()),
                () -> assertNull(global.orientationId()),
                () -> assertNull(global.orientationTitle()),
                () -> assertSame(metrics, global.metrics()),
                () -> assertEquals(
                        DashboardInsightScope.ORIENTATION,
                        orientation.scope()
                ),
                () -> assertEquals(7L, orientation.orientationId()),
                () -> assertEquals(
                        "Eficiência operacional",
                        orientation.orientationTitle()
                ),
                () -> assertSame(metrics, orientation.metrics())
        );
    }

    @Test
    void shouldRejectInconsistentOrEmptyDashboardContexts() {
        DashboardSummaryResponse metrics = metrics(1);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new DashboardInsightContext(
                                DashboardInsightScope.GLOBAL,
                                7L,
                                null,
                                metrics
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new DashboardInsightContext(
                                DashboardInsightScope.GLOBAL,
                                null,
                                "Orientação indevida",
                                metrics
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> DashboardInsightContext.orientation(
                                null,
                                "Orientação",
                                metrics
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> DashboardInsightContext.global(metrics(0))
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> DashboardInsightContext.global(null)
                )
        );
    }

    @Test
    void shouldNormalizeAndDefensivelyCopyAValidProviderResult() {
        List<String> highlights = new ArrayList<>(List.of("  Retorno positivo  "));
        List<String> attentionPoints = new ArrayList<>();
        List<String> recommendations = new ArrayList<>(
                List.of("  Acompanhar projetos em andamento  ")
        );

        DashboardInsightResult result = new DashboardInsightResult(
                "pt-BR",
                "  O portfólio apresenta resultado positivo.  ",
                highlights,
                attentionPoints,
                recommendations
        );
        highlights.add("Alteração externa");
        recommendations.clear();

        assertAll(
                () -> assertEquals("pt-BR", result.language()),
                () -> assertEquals(
                        "O portfólio apresenta resultado positivo.",
                        result.executiveSummary()
                ),
                () -> assertEquals(List.of("Retorno positivo"), result.highlights()),
                () -> assertEquals(List.of(), result.attentionPoints()),
                () -> assertEquals(
                        List.of("Acompanhar projetos em andamento"),
                        result.recommendations()
                ),
                () -> assertThrows(
                        UnsupportedOperationException.class,
                        () -> result.highlights().add("Novo item")
                )
        );
    }

    @Test
    void shouldRejectInvalidProviderResultShapeAndLimits() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result("en-US", "Resumo", list(1), list(0), list(1))
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result("pt-BR", " ", list(1), list(0), list(1))
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result(
                                "pt-BR",
                                "a".repeat(801),
                                list(1),
                                list(0),
                                list(1)
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result("pt-BR", "Resumo", list(0), list(0), list(1))
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result("pt-BR", "Resumo", list(4), list(0), list(1))
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result("pt-BR", "Resumo", list(1), list(4), list(1))
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result("pt-BR", "Resumo", list(1), list(0), list(0))
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result(
                                "pt-BR",
                                "Resumo",
                                List.of("a".repeat(301)),
                                list(0),
                                list(1)
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> result(
                                "pt-BR",
                                "Resumo",
                                List.of(" "),
                                list(0),
                                list(1)
                        )
                )
        );
    }

    @Test
    void shouldBuildAndSerializeTheStableApplicationResponse() throws Exception {
        DashboardInsightContext context = DashboardInsightContext.orientation(
                7L,
                "Eficiência operacional",
                metrics(2)
        );
        DashboardInsightResponse response = DashboardInsightResponse.from(
                context,
                validResult(),
                GENERATED_AT
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertAll(
                () -> assertEquals(8, json.size()),
                () -> assertEquals("ORIENTATION", json.get("scope").stringValue()),
                () -> assertEquals(7L, json.get("orientationId").longValue()),
                () -> assertEquals(
                        2L,
                        json.get("metricsSnapshot").get("totalProjects").longValue()
                ),
                () -> assertEquals(
                        "Resumo executivo",
                        json.get("executiveSummary").stringValue()
                ),
                () -> assertEquals(1, json.get("highlights").size()),
                () -> assertEquals(0, json.get("attentionPoints").size()),
                () -> assertEquals(1, json.get("recommendations").size()),
                () -> assertEquals(
                        GENERATED_AT,
                        Instant.parse(json.get("generatedAt").stringValue())
                )
        );
    }

    @Test
    void shouldRejectMissingResponseInputsAndInconsistentScope() {
        DashboardInsightContext context = DashboardInsightContext.global(metrics(1));

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> DashboardInsightResponse.from(
                                null,
                                validResult(),
                                GENERATED_AT
                        )
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> DashboardInsightResponse.from(
                                context,
                                null,
                                GENERATED_AT
                        )
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> DashboardInsightResponse.from(
                                context,
                                validResult(),
                                null
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new DashboardInsightResponse(
                                DashboardInsightScope.GLOBAL,
                                7L,
                                metrics(1),
                                "Resumo",
                                list(1),
                                list(0),
                                list(1),
                                GENERATED_AT
                        )
                )
        );
    }

    private DashboardInsightResult validResult() {
        return result(
                "pt-BR",
                "Resumo executivo",
                List.of("Destaque"),
                List.of(),
                List.of("Sugestão")
        );
    }

    private DashboardInsightResult result(
            String language,
            String summary,
            List<String> highlights,
            List<String> attentionPoints,
            List<String> recommendations
    ) {
        return new DashboardInsightResult(
                language,
                summary,
                highlights,
                attentionPoints,
                recommendations
        );
    }

    private List<String> list(int size) {
        List<String> values = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            values.add("Item " + index);
        }
        return values;
    }

    private DashboardSummaryResponse metrics(long totalProjects) {
        return new DashboardSummaryResponse(
                totalProjects,
                totalProjects == 0
                        ? Map.of()
                        : Map.of(NomeStatusProjeto.EM_ANDAMENTO, totalProjects),
                new BigDecimal("850000.00"),
                new BigDecimal("1200000.00"),
                new BigDecimal("210000.00"),
                new BigDecimal("560000.00"),
                new BigDecimal("32.70"),
                new BigDecimal("11.40")
        );
    }
}
