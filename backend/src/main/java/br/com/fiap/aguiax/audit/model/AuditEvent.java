package br.com.fiap.aguiax.audit.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_events")
public record AuditEvent(
        @Id String id,
        AuditEventType eventType,
        Long actorUserId,
        AuditResourceType resourceType,
        String resourceId,
        Instant timestamp,
        Map<String, Object> metadata
) {
}
