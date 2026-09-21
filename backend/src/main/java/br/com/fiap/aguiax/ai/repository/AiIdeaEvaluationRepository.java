package br.com.fiap.aguiax.ai.repository;

import br.com.fiap.aguiax.ai.model.AiIdeaEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AiIdeaEvaluationRepository
        extends MongoRepository<AiIdeaEvaluation, String> {

    List<AiIdeaEvaluation> findAllByIdeaIdOrderByCreatedAtDesc(Long ideaId);

    Optional<AiIdeaEvaluation>
    findFirstByIdeaIdOrderByCreatedAtDescIdDesc(Long ideaId);

}
