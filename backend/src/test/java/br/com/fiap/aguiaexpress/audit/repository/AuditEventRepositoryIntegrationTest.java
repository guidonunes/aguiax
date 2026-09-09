package br.com.fiap.aguiaexpress.audit.repository;

import br.com.fiap.aguiaexpress.audit.model.AuditEvent;
import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(AuditService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditEventRepositoryIntegrationTest {

    private static final String TEST_DATABASE =
            "aguiaexpress_audit_test_" + UUID.randomUUID().toString().replace("-", "");

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuditService auditService;

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
    void shouldPersistAndRetrieveAuditEventIndependentlyFromMySQL() {
        auditService.registrarEvento(
                AuditEventType.IDEA_APPROVED,
                17L,
                AuditResourceType.IDEA,
                "42",
                Map.of(
                        "previousStatus", "PRIORIZADA",
                        "newStatus", "APROVADA"
                )
        );

        AuditEvent persisted = auditEventRepository.findAll().stream()
                .findFirst()
                .orElseThrow();
        AuditEvent retrievedById = auditEventRepository.findById(persisted.id()).orElseThrow();

        assertThat(mongoTemplate.collectionExists("audit_events")).isTrue();
        assertThat(retrievedById).isEqualTo(persisted);
        assertThat(persisted.id()).isNotBlank();
        assertThat(persisted.eventType()).isEqualTo(AuditEventType.IDEA_APPROVED);
        assertThat(persisted.actorUserId()).isEqualTo(17L);
        assertThat(persisted.resourceType()).isEqualTo(AuditResourceType.IDEA);
        assertThat(persisted.resourceId()).isEqualTo("42");
        assertThat(persisted.timestamp()).isNotNull();
        assertThat(persisted.metadata()).containsEntry("previousStatus", "PRIORIZADA");
        assertThat(persisted.metadata()).containsEntry("newStatus", "APROVADA");
    }
}
