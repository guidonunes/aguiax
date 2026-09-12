package br.com.fiap.aguiaexpress.ai.repository;

import br.com.fiap.aguiaexpress.ai.model.AiIdeaEvaluation;
import org.junit.jupiter.api.AfterAll;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiIdeaEvaluationRepositoryIntegrationTest {

    private static final String TEST_DATABASE =
            "aguiaexpress_ai_history_test_"
                    + UUID.randomUUID().toString().replace("-", "");

    @Autowired
    private AiIdeaEvaluationRepository repository;

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

    @AfterAll
    void cleanUp() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void shouldRetainMultipleEvaluationsOfTheSameIdeaInNewestFirstOrder() {
        Instant firstTimestamp = Instant.parse("2026-09-12T12:00:00Z");
        Instant secondTimestamp = Instant.parse("2026-09-12T13:00:00Z");

        AiIdeaEvaluation first = evaluation(
                UUID.randomUUID().toString(),
                "7.4",
                firstTimestamp
        );
        AiIdeaEvaluation second = evaluation(
                UUID.randomUUID().toString(),
                "8.6",
                secondTimestamp
        );

        repository.saveAll(List.of(first, second));

        List<AiIdeaEvaluation> history =
                repository.findAllByIdeaIdOrderByCreatedAtDesc(42L);

        assertThat(mongoTemplate.collectionExists("idea_ai_evaluations"))
                .isTrue();
        assertThat(history)
                .hasSize(2)
                .extracting(AiIdeaEvaluation::id)
                .containsExactly(second.id(), first.id());
        assertThat(history)
                .extracting(AiIdeaEvaluation::overallScore)
                .containsExactly(
                        new BigDecimal("8.6"),
                        new BigDecimal("7.4")
                );
        assertThat(history)
                .allSatisfy(evaluation -> {
                    assertThat(evaluation.ideaId()).isEqualTo(42L);
                    assertThat(evaluation.requestedByUserId()).isEqualTo(17L);
                    assertThat(evaluation.provider()).isEqualTo("gemini");
                    assertThat(evaluation.model()).isEqualTo("gemini-3.6-flash");
                });
    }

    private AiIdeaEvaluation evaluation(
            String id,
            String overallScore,
            Instant createdAt
    ) {
        return new AiIdeaEvaluation(
                id,
                42L,
                17L,
                "gemini",
                "gemini-3.6-flash",
                9,
                8,
                7,
                8,
                new BigDecimal(overallScore),
                "Avaliação histórica.",
                createdAt
        );
    }
}
