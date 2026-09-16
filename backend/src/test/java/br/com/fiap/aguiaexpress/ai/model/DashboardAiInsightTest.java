package br.com.fiap.aguiaexpress.ai.model;

import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightResult;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardInsightScope;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardAiInsightTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-09-16T15:00:00Z");

    @Test
    void shouldBuildAValidatedTraceableMongoDocument() {
        DashboardInsightContext context = DashboardInsightContext.orientation(
                9L,
                "  Crescimento sustentável  ",
                metrics()
        );
        DashboardInsightResult result = result();

        DashboardAiInsight insight = DashboardAiInsight.from(
                "  insight-1  ",
                17L,
                context,
                result,
                "  gemini  ",
                "  gemini-flash  ",
                CREATED_AT
        );

        assertAll(
                () -> assertEquals("insight-1", insight.id()),
                () -> assertEquals(17L, insight.requestedByUserId()),
                () -> assertEquals(
                        DashboardInsightScope.ORIENTATION,
                        insight.scope()
                ),
                () -> assertEquals(9L, insight.orientationId()),
                () -> assertEquals(
                        "Crescimento sustentável",
                        insight.orientationTitle()
                ),
                () -> assertSame(context.metrics(), insight.metricsSnapshot()),
                () -> assertEquals("pt-BR", insight.language()),
                () -> assertEquals("Resumo executivo", insight.executiveSummary()),
                () -> assertEquals(List.of("Destaque"), insight.highlights()),
                () -> assertEquals(List.of(), insight.attentionPoints()),
                () -> assertEquals(List.of("Sugestão"), insight.recommendations()),
                () -> assertEquals("gemini", insight.provider()),
                () -> assertEquals("gemini-flash", insight.model()),
                () -> assertEquals(CREATED_AT, insight.createdAt())
        );
    }

    @Test
    void shouldUseTheDocumentedMongoCollection() {
        Document document = DashboardAiInsight.class.getAnnotation(Document.class);

        assertEquals("dashboard_ai_insights", document.collection());
    }

    @Test
    void shouldRejectMissingTraceabilityFields() {
        DashboardInsightContext context = DashboardInsightContext.global(metrics());
        DashboardInsightResult result = result();

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> DashboardAiInsight.from(
                                " ",
                                17L,
                                context,
                                result,
                                "gemini",
                                "modelo",
                                CREATED_AT
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> DashboardAiInsight.from(
                                "id",
                                0L,
                                context,
                                result,
                                "gemini",
                                "modelo",
                                CREATED_AT
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> DashboardAiInsight.from(
                                "id",
                                17L,
                                context,
                                result,
                                " ",
                                "modelo",
                                CREATED_AT
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> DashboardAiInsight.from(
                                "id",
                                17L,
                                context,
                                result,
                                "gemini",
                                " ",
                                CREATED_AT
                        )
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> DashboardAiInsight.from(
                                "id",
                                17L,
                                context,
                                result,
                                "gemini",
                                "modelo",
                                null
                        )
                )
        );
    }

    private DashboardInsightResult result() {
        return new DashboardInsightResult(
                "pt-BR",
                "Resumo executivo",
                List.of("Destaque"),
                List.of(),
                List.of("Sugestão")
        );
    }

    private DashboardSummaryResponse metrics() {
        return new DashboardSummaryResponse(
                1,
                Map.of(NomeStatusProjeto.EM_ANDAMENTO, 1L),
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.TEN,
                BigDecimal.ONE,
                BigDecimal.ONE
        );
    }
}
