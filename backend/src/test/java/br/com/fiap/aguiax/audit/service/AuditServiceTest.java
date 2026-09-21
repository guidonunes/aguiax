package br.com.fiap.aguiax.audit.service;

import br.com.fiap.aguiax.audit.model.AuditEvent;
import br.com.fiap.aguiax.audit.model.AuditEventType;
import br.com.fiap.aguiax.audit.model.AuditResourceType;
import br.com.fiap.aguiax.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditServiceTest {

    private AuditEventRepository auditEventRepository;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditEventRepository = mock(AuditEventRepository.class);
        auditService = new AuditService(auditEventRepository);
    }

    @Test
    void shouldCreateTraceableEventWithDefensiveMetadataCopy() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("previousStatus", "EM_ANALISE");
        metadata.put("newStatus", "APROVADA");
        Instant beforeRegistration = Instant.now();

        auditService.registrarEvento(
                AuditEventType.IDEA_APPROVED,
                17L,
                AuditResourceType.IDEA,
                "42",
                metadata
        );
        Instant afterRegistration = Instant.now();
        metadata.put("newStatus", "REJEITADA");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(eventCaptor.capture());

        AuditEvent persistedEvent = eventCaptor.getValue();
        assertNotNull(persistedEvent.id());
        assertFalse(persistedEvent.id().isBlank());
        assertEquals(AuditEventType.IDEA_APPROVED, persistedEvent.eventType());
        assertEquals(17L, persistedEvent.actorUserId());
        assertEquals(AuditResourceType.IDEA, persistedEvent.resourceType());
        assertEquals("42", persistedEvent.resourceId());
        assertFalse(persistedEvent.timestamp().isBefore(beforeRegistration));
        assertFalse(persistedEvent.timestamp().isAfter(afterRegistration));
        assertEquals("APROVADA", persistedEvent.metadata().get("newStatus"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> persistedEvent.metadata().put("extra", "value")
        );
    }

    @Test
    void shouldReplaceNullMetadataWithEmptyMap() {
        auditService.registrarEvento(
                AuditEventType.PROJECT_CREATED,
                3L,
                AuditResourceType.PROJECT,
                "9",
                null
        );

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(eventCaptor.capture());
        assertEquals(Map.of(), eventCaptor.getValue().metadata());
    }

    @Test
    void shouldRejectInvalidRequiredFieldsBeforePersistence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> auditService.registrarEvento(
                        null,
                        17L,
                        AuditResourceType.IDEA,
                        "42",
                        Map.of()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> auditService.registrarEvento(
                        AuditEventType.IDEA_UPDATED,
                        0L,
                        AuditResourceType.IDEA,
                        "42",
                        Map.of()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> auditService.registrarEvento(
                        AuditEventType.IDEA_UPDATED,
                        17L,
                        null,
                        "42",
                        Map.of()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> auditService.registrarEvento(
                        AuditEventType.IDEA_UPDATED,
                        17L,
                        AuditResourceType.IDEA,
                        " ",
                        Map.of()
                )
        );

        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void shouldNotFailBusinessFlowWhenMongoDBIsUnavailable() {
        when(auditEventRepository.save(any()))
                .thenThrow(new DataAccessResourceFailureException("MongoDB unavailable"));

        assertDoesNotThrow(() -> auditService.registrarEvento(
                AuditEventType.ORIENTATION_UPDATED,
                5L,
                AuditResourceType.ORIENTATION,
                "12",
                Map.of("previousStatus", 1, "newStatus", 0)
        ));
    }
}
