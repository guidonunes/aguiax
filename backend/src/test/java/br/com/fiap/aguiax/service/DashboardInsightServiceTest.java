package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.ai.client.AiDashboardInsightClient;
import br.com.fiap.aguiax.ai.client.AiProviderException;
import br.com.fiap.aguiax.ai.client.AiProviderFailure;
import br.com.fiap.aguiax.ai.model.DashboardAiInsight;
import br.com.fiap.aguiax.ai.repository.DashboardAiInsightRepository;
import br.com.fiap.aguiax.audit.model.AuditEventType;
import br.com.fiap.aguiax.audit.model.AuditResourceType;
import br.com.fiap.aguiax.audit.service.AuditService;
import br.com.fiap.aguiax.config.ai.AiProviderProperties;
import br.com.fiap.aguiax.dto.ai.DashboardInsightContext;
import br.com.fiap.aguiax.dto.ai.DashboardInsightResult;
import br.com.fiap.aguiax.dto.dashboard.DashboardInsightResponse;
import br.com.fiap.aguiax.dto.dashboard.DashboardInsightScope;
import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiax.exception.DashboardInsightPersistenceException;
import br.com.fiap.aguiax.exception.DashboardInsightUnavailableException;
import br.com.fiap.aguiax.exception.InvalidRequestException;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.model.Usuario;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DashboardInsightServiceTest {

    private DashboardService dashboardService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private ObjectProvider<AiDashboardInsightClient> clientProvider;
    private AiDashboardInsightClient client;
    private DashboardAiInsightRepository insightRepository;
    private AuditService auditService;
    private DashboardInsightService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        dashboardService = mock(DashboardService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        clientProvider = mock(ObjectProvider.class);
        client = mock(AiDashboardInsightClient.class);
        insightRepository = mock(DashboardAiInsightRepository.class);
        auditService = mock(AuditService.class);

        service = new DashboardInsightService(
                dashboardService,
                usuarioAutenticadoService,
                clientProvider,
                providerProperties(),
                insightRepository,
                auditService
        );
    }

    @Test
    void shouldAnalyzePersistAndAuditTheExactTrustedGlobalSnapshot() {
        DashboardSummaryResponse metrics = metrics(2);
        DashboardInsightResult result = result();
        when(dashboardService.buscarResumo(null)).thenReturn(metrics);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.generateInsight(any())).thenReturn(result);

        DashboardInsightResponse response = service.gerarInsight(null);

        ArgumentCaptor<DashboardInsightContext> contextCaptor =
                ArgumentCaptor.forClass(DashboardInsightContext.class);
        verify(client).generateInsight(contextCaptor.capture());
        DashboardInsightContext context = contextCaptor.getValue();

        ArgumentCaptor<DashboardAiInsight> persistedCaptor =
                ArgumentCaptor.forClass(DashboardAiInsight.class);
        verify(insightRepository).save(persistedCaptor.capture());
        DashboardAiInsight persisted = persistedCaptor.getValue();

        assertAll(
                () -> assertEquals(DashboardInsightScope.GLOBAL, context.scope()),
                () -> assertNull(context.orientationId()),
                () -> assertSame(metrics, context.metrics()),
                () -> assertEquals(DashboardInsightScope.GLOBAL, response.scope()),
                () -> assertNull(response.orientationId()),
                () -> assertSame(metrics, response.metricsSnapshot()),
                () -> assertEquals(result.executiveSummary(), response.executiveSummary()),
                () -> assertEquals(result.highlights(), response.highlights()),
                () -> assertEquals(result.attentionPoints(), response.attentionPoints()),
                () -> assertEquals(result.recommendations(), response.recommendations()),
                () -> assertNotNull(response.generatedAt()),
                () -> assertEquals(17L, persisted.requestedByUserId()),
                () -> assertEquals(DashboardInsightScope.GLOBAL, persisted.scope()),
                () -> assertSame(metrics, persisted.metricsSnapshot()),
                () -> assertEquals("gemini", persisted.provider()),
                () -> assertEquals("gemini-3.6-flash", persisted.model()),
                () -> assertEquals(response.generatedAt(), persisted.createdAt()),
                () -> assertEquals(36, persisted.id().length())
        );

        InOrder order = inOrder(auditService, client, insightRepository);
        order.verify(auditService).registrarEvento(
                eq(AuditEventType.DASHBOARD_INSIGHT_REQUESTED),
                eq(17L),
                eq(AuditResourceType.DASHBOARD_INSIGHT),
                anyString(),
                any()
        );
        order.verify(client).generateInsight(any());
        order.verify(insightRepository).save(any());
        order.verify(auditService).registrarEvento(
                eq(AuditEventType.DASHBOARD_INSIGHT_COMPLETED),
                eq(17L),
                eq(AuditResourceType.DASHBOARD_INSIGHT),
                anyString(),
                any()
        );

        ArgumentCaptor<String> resourceIdCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> metadataCaptor = mapCaptor();
        verify(auditService, org.mockito.Mockito.times(2)).registrarEvento(
                any(),
                eq(17L),
                eq(AuditResourceType.DASHBOARD_INSIGHT),
                resourceIdCaptor.capture(),
                metadataCaptor.capture()
        );
        assertEquals(
                resourceIdCaptor.getAllValues().get(0),
                resourceIdCaptor.getAllValues().get(1)
        );
        metadataCaptor.getAllValues().forEach(metadata -> assertAll(
                () -> assertEquals("GLOBAL", metadata.get("scope")),
                () -> assertEquals("gemini", metadata.get("provider")),
                () -> assertEquals("gemini-3.6-flash", metadata.get("model")),
                () -> assertFalse(metadata.containsKey("executiveSummary")),
                () -> assertFalse(metadata.containsKey("prompt"))
        ));
        verify(auditService, never()).registrarEvento(
                eq(AuditEventType.DASHBOARD_INSIGHT_FAILED),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldPreserveTheTrustedOrientationFilterInContextHistoryAndResponse() {
        DashboardSummaryResponse metrics = metrics(2);
        when(dashboardService.buscarResumo(42L)).thenReturn(metrics);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.generateInsight(any())).thenReturn(result());

        DashboardInsightResponse response = service.gerarInsight(42L);

        verify(client).generateInsight(argThat(argThatContext -> {
            assertEquals(DashboardInsightScope.ORIENTATION, argThatContext.scope());
            assertEquals(42L, argThatContext.orientationId());
            assertNull(argThatContext.orientationTitle());
            assertSame(metrics, argThatContext.metrics());
            return true;
        }));
        verify(insightRepository).save(any(DashboardAiInsight.class));
        assertAll(
                () -> assertEquals(DashboardInsightScope.ORIENTATION, response.scope()),
                () -> assertEquals(42L, response.orientationId()),
                () -> assertSame(metrics, response.metricsSnapshot())
        );
        verify(auditService, org.mockito.Mockito.times(2)).registrarEvento(
                any(),
                eq(17L),
                eq(AuditResourceType.DASHBOARD_INSIGHT),
                anyString(),
                org.mockito.ArgumentMatchers.argThat(metadata ->
                        Long.valueOf(42L).equals(metadata.get("orientationId"))
                                && "ORIENTATION".equals(metadata.get("scope"))
                )
        );
    }

    @Test
    void shouldRejectAnEmptySnapshotBeforeAuthenticationProviderOrAudit() {
        when(dashboardService.buscarResumo(null)).thenReturn(metrics(0));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> service.gerarInsight(null)
        );

        assertEquals(
                "Não há projetos no dashboard para gerar uma análise executiva.",
                exception.getMessage()
        );
        verifyNoInteractions(
                usuarioAutenticadoService,
                clientProvider,
                client,
                insightRepository,
                auditService
        );
    }

    @Test
    void shouldPreserveUnknownOrientationFailureBeforeProviderOrAudit() {
        ResourceNotFoundException notFound = new ResourceNotFoundException(
                "Orientação estratégica não encontrada."
        );
        when(dashboardService.buscarResumo(404L)).thenThrow(notFound);

        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> service.gerarInsight(404L)
        );

        assertSame(notFound, thrown);
        verifyNoInteractions(
                usuarioAutenticadoService,
                clientProvider,
                client,
                insightRepository,
                auditService
        );
    }

    @Test
    void shouldReturnControlledFailureAndAuditWhenAiIsDisabled() {
        when(dashboardService.buscarResumo(null)).thenReturn(metrics(2));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(17L));
        when(clientProvider.getIfAvailable()).thenReturn(null);

        DashboardInsightUnavailableException exception = assertThrows(
                DashboardInsightUnavailableException.class,
                () -> service.gerarInsight(null)
        );

        assertEquals(
                "A análise executiva por IA não está habilitada.",
                exception.getMessage()
        );
        verifyNoInteractions(client, insightRepository);
        verifyFailedAudit("AI_DISABLED");
    }

    @Test
    void shouldPropagateProviderFailureWithoutPersistingAndAuditSafeMetadata() {
        AiProviderException providerFailure = new AiProviderException(
                AiProviderFailure.TIMEOUT,
                "x-goog-api-key=never-log-this-key"
        );
        when(dashboardService.buscarResumo(null)).thenReturn(metrics(2));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.generateInsight(any())).thenThrow(providerFailure);

        Logger logger = (Logger) LoggerFactory.getLogger(
                DashboardInsightService.class
        );
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            AiProviderException thrown = assertThrows(
                    AiProviderException.class,
                    () -> service.gerarInsight(null)
            );
            assertSame(providerFailure, thrown);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        String logs = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .reduce("", (left, right) -> left + "\n" + right);
        assertTrue(logs.contains("provider=gemini, failureType=TIMEOUT"));
        assertFalse(logs.contains("never-log-this-key"));
        assertFalse(logs.contains("x-goog-api-key"));
        verifyNoInteractions(insightRepository);
        verifyFailedAudit("TIMEOUT");
    }

    @Test
    void shouldFailWithoutCompletedAuditWhenMongoHistoryCannotBeStored() {
        when(dashboardService.buscarResumo(null)).thenReturn(metrics(2));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.generateInsight(any())).thenReturn(result());
        when(insightRepository.save(any())).thenThrow(
                new DataAccessResourceFailureException("mongo unavailable")
        );

        DashboardInsightPersistenceException exception = assertThrows(
                DashboardInsightPersistenceException.class,
                () -> service.gerarInsight(null)
        );

        assertEquals(
                "Não foi possível armazenar o histórico da análise executiva.",
                exception.getMessage()
        );
        verifyFailedAudit("PERSISTENCE_UNAVAILABLE");
        verify(auditService, never()).registrarEvento(
                eq(AuditEventType.DASHBOARD_INSIGHT_COMPLETED),
                any(),
                any(),
                any(),
                any()
        );
    }

    private void verifyFailedAudit(String failureType) {
        verify(auditService).registrarEvento(
                eq(AuditEventType.DASHBOARD_INSIGHT_FAILED),
                eq(17L),
                eq(AuditResourceType.DASHBOARD_INSIGHT),
                anyString(),
                org.mockito.ArgumentMatchers.argThat(metadata ->
                        failureType.equals(metadata.get("failureType"))
                                && !metadata.containsKey("executiveSummary")
                                && !metadata.containsKey("prompt")
                )
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }

    private DashboardSummaryResponse metrics(long totalProjects) {
        Map<NomeStatusProjeto, Long> statuses = totalProjects == 0
                ? Map.of()
                : Map.of(
                        NomeStatusProjeto.PLANEJADO, 1L,
                        NomeStatusProjeto.EM_ANDAMENTO, totalProjects - 1
                );

        return new DashboardSummaryResponse(
                totalProjects,
                statuses,
                new BigDecimal("300.00"),
                new BigDecimal("450.00"),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("15.00"),
                new BigDecimal("10.00")
        );
    }

    private DashboardInsightResult result() {
        return new DashboardInsightResult(
                "pt-BR",
                "O portfólio apresenta resultado estimado positivo.",
                List.of("O retorno supera o investimento."),
                List.of("Um projeto ainda está em andamento."),
                List.of("Acompanhar a realização do retorno.")
        );
    }

    private Usuario user(Long id) {
        Usuario user = new Usuario();
        user.setId(id);
        return user;
    }

    private AiProviderProperties providerProperties() {
        return new AiProviderProperties(
                true,
                "gemini",
                "test-key",
                "gemini-3.6-flash",
                "https://generativelanguage.googleapis.com/v1beta",
                Duration.ofSeconds(3),
                Duration.ofSeconds(15)
        );
    }
}
