package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.ai.model.AiIdeaEvaluation;
import br.com.fiap.aguiaexpress.ai.repository.AiIdeaEvaluationRepository;
import br.com.fiap.aguiaexpress.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiaexpress.model.Ideia;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IdeaPriorityService {

    private final AiIdeaEvaluationRepository evaluationRepository;
    private final IdeaPriorityCalculator calculator;

    public BigDecimal recalculateWithLatestEvaluation(Ideia ideia) {
        validateIdea(ideia);

        BigDecimal latestAiScore;

        try {
            latestAiScore = evaluationRepository
                    .findFirstByIdeaIdOrderByCreatedAtDescIdDesc(ideia.getId())
                    .map(AiIdeaEvaluation::overallScore)
                    .orElse(BigDecimal.ZERO);
        } catch (DataAccessException exception) {
            throw new PriorityCalculationUnavailableException(
                    "Não foi possível consultar a avaliação de IA da ideia.",
                    exception
            );
        }

        return recalculate(ideia, latestAiScore);
    }

    public BigDecimal recalculate(
            Ideia ideia,
            BigDecimal aiScore
    ) {
        validateIdea(ideia);

        BigDecimal priority = calculator.calculate(
                ideia.getTotalVotos(),
                ideia.getImpacto(),
                ideia.getViabilidade(),
                aiScore
        );

        ideia.setPrioridade(priority);
        return priority;
    }

    private void validateIdea(Ideia ideia) {
        if (ideia == null || ideia.getId() == null || ideia.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Uma ideia persistida é obrigatória para calcular a prioridade"
            );
        }
    }
}
