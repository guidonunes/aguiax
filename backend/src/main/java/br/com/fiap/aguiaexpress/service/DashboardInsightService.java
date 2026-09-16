package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.ai.client.AiDashboardInsightClient;
import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.ai.model.DashboardAiInsight;
import br.com.fiap.aguiaexpress.ai.repository.DashboardAiInsightRepository;
import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.config.ai.AiProviderProperties;
import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiaexpress.dto.ai.DashboardInsightResult;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardInsightResponse;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.exception.DashboardInsightPersistenceException;
import br.com.fiap.aguiaexpress.exception.DashboardInsightUnavailableException;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardInsightService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            DashboardInsightService.class
    );

    private final DashboardService dashboardService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ObjectProvider<AiDashboardInsightClient> clientProvider;
    private final AiProviderProperties providerProperties;
    private final DashboardAiInsightRepository insightRepository;
    private final AuditService auditService;

    public DashboardInsightResponse gerarInsight(Long orientationId) {
        DashboardSummaryResponse metrics = dashboardService.buscarResumo(
                orientationId
        );
        validarSnapshot(metrics);

        DashboardInsightContext context = criarContexto(
                orientationId,
                metrics
        );
        Usuario actor = usuarioAutenticadoService.getUsuarioAutenticado();
        String insightId = UUID.randomUUID().toString();

        registrarEvento(
                AuditEventType.DASHBOARD_INSIGHT_REQUESTED,
                actor.getId(),
                insightId,
                metadadosBase(context)
        );

        try {
            AiDashboardInsightClient client = clientProvider.getIfAvailable();
            if (client == null) {
                throw new DashboardInsightUnavailableException(
                        "A análise executiva por IA não está habilitada."
                );
            }

            DashboardInsightResult result = client.generateInsight(context);
            Instant generatedAt = Instant.now();
            DashboardInsightResponse response = DashboardInsightResponse.from(
                    context,
                    result,
                    generatedAt
            );

            persistirInsight(
                    insightId,
                    actor.getId(),
                    context,
                    result,
                    generatedAt
            );

            registrarEvento(
                    AuditEventType.DASHBOARD_INSIGHT_COMPLETED,
                    actor.getId(),
                    insightId,
                    metadadosBase(context)
            );
            return response;
        } catch (RuntimeException exception) {
            String failureType = tipoDaFalha(exception);
            LOGGER.warn(
                    "Falha na análise executiva por IA: scope={}, orientationId={}, "
                            + "provider={}, failureType={}",
                    context.scope(),
                    context.orientationId(),
                    providerProperties.provider(),
                    failureType
            );

            Map<String, Object> metadata = metadadosBase(context);
            metadata.put("failureType", failureType);
            registrarEvento(
                    AuditEventType.DASHBOARD_INSIGHT_FAILED,
                    actor.getId(),
                    insightId,
                    metadata
            );
            throw exception;
        }
    }

    private void validarSnapshot(DashboardSummaryResponse metrics) {
        if (metrics.totalProjects() == 0) {
            throw new InvalidRequestException(
                    "Não há projetos no dashboard para gerar uma análise executiva."
            );
        }
    }

    private DashboardInsightContext criarContexto(
            Long orientationId,
            DashboardSummaryResponse metrics
    ) {
        if (orientationId == null) {
            return DashboardInsightContext.global(metrics);
        }

        return DashboardInsightContext.orientation(
                orientationId,
                null,
                metrics
        );
    }

    private void persistirInsight(
            String insightId,
            Long actorId,
            DashboardInsightContext context,
            DashboardInsightResult result,
            Instant generatedAt
    ) {
        DashboardAiInsight insight = DashboardAiInsight.from(
                insightId,
                actorId,
                context,
                result,
                providerProperties.provider(),
                providerProperties.model(),
                generatedAt
        );

        try {
            insightRepository.save(insight);
        } catch (DataAccessException exception) {
            throw new DashboardInsightPersistenceException(
                    "Não foi possível armazenar o histórico da análise executiva.",
                    exception
            );
        }
    }

    private Map<String, Object> metadadosBase(
            DashboardInsightContext context
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("scope", context.scope().name());
        if (context.orientationId() != null) {
            metadata.put("orientationId", context.orientationId());
        }
        metadata.put("provider", providerProperties.provider());
        metadata.put("model", providerProperties.model());
        return metadata;
    }

    private String tipoDaFalha(RuntimeException exception) {
        if (exception instanceof AiProviderException providerException) {
            return providerException.failure().name();
        }
        if (exception instanceof DashboardInsightUnavailableException) {
            return "AI_DISABLED";
        }
        if (exception instanceof DashboardInsightPersistenceException) {
            return "PERSISTENCE_UNAVAILABLE";
        }

        return exception.getClass().getSimpleName();
    }

    private void registrarEvento(
            AuditEventType eventType,
            Long actorId,
            String insightId,
            Map<String, Object> metadata
    ) {
        auditService.registrarEvento(
                eventType,
                actorId,
                AuditResourceType.DASHBOARD_INSIGHT,
                insightId,
                metadata
        );
    }
}
