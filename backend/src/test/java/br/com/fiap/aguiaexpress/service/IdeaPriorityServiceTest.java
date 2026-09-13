package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.ai.model.AiIdeaEvaluation;
import br.com.fiap.aguiaexpress.ai.repository.AiIdeaEvaluationRepository;
import br.com.fiap.aguiaexpress.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdeaPriorityServiceTest {

    private AiIdeaEvaluationRepository evaluationRepository;
    private IdeaPriorityCalculator calculator;
    private IdeaPriorityService service;

    @BeforeEach
    void setUp() {
        evaluationRepository = mock(AiIdeaEvaluationRepository.class);
        calculator = new IdeaPriorityCalculator();
        service = new IdeaPriorityService(evaluationRepository, calculator);
    }

    @Test
    void shouldApplyTheMostRecentCompletedAiEvaluation() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        when(evaluationRepository
                .findFirstByIdeaIdOrderByCreatedAtDescIdDesc(42L))
                .thenReturn(Optional.of(evaluation("9.0")));

        BigDecimal result = service.recalculateWithLatestEvaluation(ideia);

        assertEquals(new BigDecimal("7.35"), result);
        assertEquals(new BigDecimal("7.35"), ideia.getPrioridade());
        assertSame(originalStatus, ideia.getStatusIdeia());
    }

    @Test
    void shouldUseZeroWhenTheIdeaHasNoAiEvaluation() {
        Ideia ideia = ideia();
        when(evaluationRepository
                .findFirstByIdeaIdOrderByCreatedAtDescIdDesc(42L))
                .thenReturn(Optional.empty());

        assertEquals(
                new BigDecimal("4.20"),
                service.recalculateWithLatestEvaluation(ideia)
        );
    }

    @Test
    void shouldUseTheJustPersistedAiScoreWithoutAnotherMongoQuery() {
        Ideia ideia = ideia();

        BigDecimal result = service.recalculate(
                ideia,
                new BigDecimal("8.10")
        );

        assertEquals(new BigDecimal("7.04"), result);
        verify(
                evaluationRepository,
                never()
        ).findFirstByIdeaIdOrderByCreatedAtDescIdDesc(42L);
    }

    @Test
    void shouldFailSafelyWhenLatestEvaluationCannotBeRead() {
        Ideia ideia = ideia();
        BigDecimal originalPriority = ideia.getPrioridade();
        when(evaluationRepository
                .findFirstByIdeaIdOrderByCreatedAtDescIdDesc(42L))
                .thenThrow(new DataAccessResourceFailureException(
                        "mongo unavailable"
                ));

        PriorityCalculationUnavailableException exception = assertThrows(
                PriorityCalculationUnavailableException.class,
                () -> service.recalculateWithLatestEvaluation(ideia)
        );

        assertEquals(
                "Não foi possível consultar a avaliação de IA da ideia.",
                exception.getMessage()
        );
        assertSame(originalPriority, ideia.getPrioridade());
    }

    @Test
    void shouldRejectAnUnpersistedIdea() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recalculateWithLatestEvaluation(new Ideia())
        );
    }

    private Ideia ideia() {
        Ideia ideia = new Ideia();
        ideia.setId(42L);
        ideia.setTotalVotos(5);
        ideia.setImpacto(8);
        ideia.setViabilidade(6);
        ideia.setPrioridade(new BigDecimal("1.00"));
        ideia.setStatusIdeia(new StatusIdeia());
        return ideia;
    }

    private AiIdeaEvaluation evaluation(String overallScore) {
        return new AiIdeaEvaluation(
                "123e4567-e89b-12d3-a456-426614174000",
                42L,
                17L,
                "gemini",
                "gemini-3.6-flash",
                9,
                8,
                7,
                8,
                new BigDecimal(overallScore),
                "Resumo validado.",
                Instant.parse("2026-09-12T18:00:00Z")
        );
    }
}
