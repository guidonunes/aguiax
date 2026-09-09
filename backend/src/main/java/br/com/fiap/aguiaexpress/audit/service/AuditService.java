package br.com.fiap.aguiaexpress.audit.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEvent;
import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;

    public void registrarEvento(
            AuditEventType eventType,
            Long actorUserId,
            AuditResourceType resourceType,
            String resourceId,
            Map<String, Object> metadata
    ) {
        validarCamposObrigatorios(eventType, actorUserId, resourceType, resourceId);

        AuditEvent event = new AuditEvent(
                UUID.randomUUID().toString(),
                eventType,
                actorUserId,
                resourceType,
                resourceId,
                Instant.now(),
                copiarMetadata(metadata)
        );

        try {
            auditEventRepository.save(event);
        } catch (DataAccessException exception) {
            LOGGER.error(
                    "Falha ao persistir evento de auditoria: "
                            + "eventType={}, resourceType={}, resourceId={}, failureType={}",
                    eventType,
                    resourceType,
                    resourceId,
                    exception.getClass().getSimpleName()
            );
        }
    }

    private void validarCamposObrigatorios(
            AuditEventType eventType,
            Long actorUserId,
            AuditResourceType resourceType,
            String resourceId
    ) {
        if (eventType == null) {
            throw new IllegalArgumentException("O tipo do evento de auditoria é obrigatório.");
        }
        if (actorUserId == null || actorUserId <= 0) {
            throw new IllegalArgumentException("O identificador do ator deve ser positivo.");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("O tipo do recurso auditado é obrigatório.");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("O identificador do recurso auditado é obrigatório.");
        }
    }

    private Map<String, Object> copiarMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }
}
