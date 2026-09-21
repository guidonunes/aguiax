package br.com.fiap.aguiax.ai.repository;

import br.com.fiap.aguiax.ai.model.DashboardAiInsight;
import br.com.fiap.aguiax.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiax.dto.ai.DashboardInsightResult;
import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DashboardAiInsightRepositoryIntegrationTest {

    private static final String TEST_DATABASE =
            "agx_dashboard_insight_test_"
                    + UUID.randomUUID().toString().replace("-", "");

    @Autowired
    private DashboardAiInsightRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void configureMongoDB(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.mongodb.uri",
                () -> System.getProperty(
                        "mongodb.test.uri",
                        "mongodb://localhost:27017"
                )
        );
        registry.add("spring.mongodb.database", () -> TEST_DATABASE);
    }

    @BeforeEach
    void clearInsights() {
        repository.deleteAll();
    }

    @AfterAll
    void cleanUp() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void shouldPersistTheCompleteTrustedSnapshotAndValidatedInsight() {
        String id = UUID.randomUUID().toString();
        DashboardSummaryResponse metrics = metrics();
        DashboardInsightResult result = result();
        Instant createdAt = Instant.parse("2026-09-16T15:00:00Z");

        repository.save(DashboardAiInsight.from(
                id,
                17L,
                DashboardInsightContext.orientation(
                        42L,
                        "Eficiência operacional",
                        metrics
                ),
                result,
                "gemini",
                "gemini-3.6-flash",
                createdAt
        ));

        DashboardAiInsight stored = repository.findById(id).orElseThrow();

        assertThat(mongoTemplate.collectionExists("dashboard_ai_insights"))
                .isTrue();
        assertThat(stored.requestedByUserId()).isEqualTo(17L);
        assertThat(stored.orientationId()).isEqualTo(42L);
        assertThat(stored.orientationTitle()).isEqualTo("Eficiência operacional");
        assertThat(stored.metricsSnapshot()).isEqualTo(metrics);
        assertThat(stored.executiveSummary()).isEqualTo(result.executiveSummary());
        assertThat(stored.highlights()).containsExactlyElementsOf(result.highlights());
        assertThat(stored.attentionPoints())
                .containsExactlyElementsOf(result.attentionPoints());
        assertThat(stored.recommendations())
                .containsExactlyElementsOf(result.recommendations());
        assertThat(stored.provider()).isEqualTo("gemini");
        assertThat(stored.model()).isEqualTo("gemini-3.6-flash");
        assertThat(stored.createdAt()).isEqualTo(createdAt);
    }

    private DashboardSummaryResponse metrics() {
        return new DashboardSummaryResponse(
                2,
                Map.of(
                        NomeStatusProjeto.PLANEJADO, 1L,
                        NomeStatusProjeto.EM_ANDAMENTO, 1L
                ),
                new BigDecimal("300.00"),
                new BigDecimal("450.00"),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("15.00"),
                new BigDecimal("10.00")
        );
    }

    private DashboardInsightResult result() {
        return new DashboardInsightResult(
                "pt-BR",
                "O portfólio apresenta resultado estimado positivo.",
                List.of("O retorno supera o investimento."),
                List.of("Um projeto ainda está em andamento."),
                List.of("Acompanhar a realização do retorno.")
        );
    }
}
