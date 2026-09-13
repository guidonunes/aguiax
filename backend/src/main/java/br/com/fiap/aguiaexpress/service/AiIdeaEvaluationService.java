package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.ai.client.AiIdeaEvaluationClient;
import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.ai.model.AiIdeaEvaluation;
import br.com.fiap.aguiaexpress.ai.repository.AiIdeaEvaluationRepository;
import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.config.ai.AiProviderProperties;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationContext;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResponse;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResult;
import br.com.fiap.aguiaexpress.exception.AiEvaluationPersistenceException;
import br.com.fiap.aguiaexpress.exception.AiEvaluationUnavailableException;
import br.com.fiap.aguiaexpress.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiIdeaEvaluationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AiIdeaEvaluationService.class
    );

    private final IdeiaService ideiaService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ObjectProvider<AiIdeaEvaluationClient> clientProvider;
    private final AiProviderProperties providerProperties;
    private final AiIdeaEvaluationRepository evaluationRepository;
    private final AuditService auditService;

    public AiIdeaEvaluationResponse avaliarIdeia(Long ideiaId) {
        Ideia ideia = ideiaService.buscarEntidadePorId(ideiaId);
        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();

        registrarEvento(
                AuditEventType.AI_EVALUATION_REQUESTED,
                actor.getId(),
                ideia.getId(),
                metadadosDoProvedor()
        );

        try {
            AiIdeaEvaluationClient client = clientProvider.getIfAvailable();
            if (client == null) {
                throw new AiEvaluationUnavailableException(
                        "A avaliação por IA não está habilitada."
                );
            }

            AiIdeaEvaluationResult result = client.evaluate(criarContexto(ideia));
            AiIdeaEvaluationResponse response = AiIdeaEvaluationResponse.from(
                    ideia.getId(),
                    result
            );
            persistirAvaliacao(ideia.getId(), actor.getId(), result);

            BigDecimal priorityScore =
                    ideiaService.atualizarPrioridadeComPontuacaoAi(
                            ideia,
                            result.overallScore()
                    );

            Map<String, Object> metadata = metadadosDoProvedor();
            metadata.put("overallScore", response.overallScore());
            metadata.put("priorityScore", priorityScore);

            registrarEvento(
                    AuditEventType.AI_EVALUATION_COMPLETED,
                    actor.getId(),
                    ideia.getId(),
                    metadata
            );

            return response;
        } catch (RuntimeException exception) {
            String failureType = tipoDaFalha(exception);
            LOGGER.warn(
                    "Falha na avaliação de IA: ideaId={}, provider={}, failureType={}",
                    ideia.getId(),
                    providerProperties.provider(),
                    failureType
            );

            Map<String, Object> metadata = metadadosDoProvedor();
            metadata.put("failureType", failureType);
            registrarEvento(
                    AuditEventType.AI_EVALUATION_FAILED,
                    actor.getId(),
                    ideia.getId(),
                    metadata
            );
            throw exception;
        }
    }

    private AiIdeaEvaluationContext criarContexto(Ideia ideia) {
        return new AiIdeaEvaluationContext(
                ideia.getTitulo(),
                ideia.getDescricao(),
                ideia.getProblema(),
                ideia.getSolucaoProposta(),
                ideia.getCategoria().getNomeCategoria(),
                descreverOrientacao(ideia.getOrientacaoEstrategica()),
                ideia.getImpacto(),
                ideia.getViabilidade()
        );
    }

    private String descreverOrientacao(OrientacaoEstrategica orientacao) {
        if (orientacao.getDescricao() == null
                || orientacao.getDescricao().isBlank()) {
            return orientacao.getTitulo();
        }

        return orientacao.getTitulo() + " — " + orientacao.getDescricao();
    }

    private Map<String, Object> metadadosDoProvedor() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("provider", providerProperties.provider());
        metadata.put("model", providerProperties.model());
        return metadata;
    }

    private String tipoDaFalha(RuntimeException exception) {
        if (exception instanceof AiProviderException providerException) {
            return providerException.failure().name();
        }
        if (exception instanceof AiEvaluationUnavailableException) {
            return "AI_DISABLED";
        }
        if (exception instanceof AiEvaluationPersistenceException) {
            return "PERSISTENCE_UNAVAILABLE";
        }
        if (exception instanceof PriorityCalculationUnavailableException) {
            return "PRIORITY_UNAVAILABLE";
        }

        return exception.getClass().getSimpleName();
    }

    private void persistirAvaliacao(
            Long ideiaId,
            Long actorId,
            AiIdeaEvaluationResult result
    ) {
        AiIdeaEvaluation evaluation = new AiIdeaEvaluation(
                UUID.randomUUID().toString(),
                ideiaId,
                actorId,
                providerProperties.provider(),
                providerProperties.model(),
                result.strategicAlignment(),
                result.impact(),
                result.feasibility(),
                result.innovation(),
                result.overallScore(),
                result.summary(),
                Instant.now()
        );

        try {
            evaluationRepository.save(evaluation);
        } catch (DataAccessException exception) {
            throw new AiEvaluationPersistenceException(
                    "Não foi possível armazenar o histórico da avaliação de IA.",
                    exception
            );
        }
    }

    private void registrarEvento(
            AuditEventType eventType,
            Long actorId,
            Long ideiaId,
            Map<String, Object> metadata
    ) {
        auditService.registrarEvento(
                eventType,
                actorId,
                AuditResourceType.AI_EVALUATION,
                String.valueOf(ideiaId),
                metadata
        );
    }
}
