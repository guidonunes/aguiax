package br.com.fiap.aguiax.audit.repository;

import br.com.fiap.aguiax.audit.model.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
