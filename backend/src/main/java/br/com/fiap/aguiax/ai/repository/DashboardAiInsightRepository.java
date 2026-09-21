package br.com.fiap.aguiax.ai.repository;

import br.com.fiap.aguiax.ai.model.DashboardAiInsight;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DashboardAiInsightRepository
        extends MongoRepository<DashboardAiInsight, String> {
}
