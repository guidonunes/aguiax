package br.com.fiap.aguiaexpress.ai.repository;

import br.com.fiap.aguiaexpress.ai.model.AiIdeaEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AiIdeaEvaluationRepository
        extends MongoRepository<AiIdeaEvaluation, String> {

    List<AiIdeaEvaluation> findAllByIdeaIdOrderByCreatedAtDesc(Long ideaId);
}
