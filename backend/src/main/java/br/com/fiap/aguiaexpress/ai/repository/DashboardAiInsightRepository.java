package br.com.fiap.aguiaexpress.ai.repository;

import br.com.fiap.aguiaexpress.ai.model.DashboardAiInsight;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DashboardAiInsightRepository
        extends MongoRepository<DashboardAiInsight, String> {
}
