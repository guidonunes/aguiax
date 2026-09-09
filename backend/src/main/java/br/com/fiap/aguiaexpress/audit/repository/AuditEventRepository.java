package br.com.fiap.aguiaexpress.audit.repository;

import br.com.fiap.aguiaexpress.audit.model.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
