package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.ai.client.AiIdeaEvaluationClient;
import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.ai.client.AiProviderFailure;
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
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.Categoria;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.Usuario;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AiIdeaEvaluationServiceTest {

    private IdeiaService ideiaService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private ObjectProvider<AiIdeaEvaluationClient> clientProvider;
    private AiIdeaEvaluationClient client;
    private AiIdeaEvaluationRepository evaluationRepository;
    private AuditService auditService;
    private AiIdeaEvaluationService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ideiaService = mock(IdeiaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        clientProvider = mock(ObjectProvider.class);
        client = mock(AiIdeaEvaluationClient.class);
        evaluationRepository = mock(AiIdeaEvaluationRepository.class);
        auditService = mock(AuditService.class);

        service = new AiIdeaEvaluationService(
                ideiaService,
                usuarioAutenticadoService,
                clientProvider,
                providerProperties(),
                evaluationRepository,
                auditService
        );
    }

    @Test
    void shouldBuildTrustedContextAndReturnStructuredEvaluation() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        Usuario actor = usuario(17L);
        AiIdeaEvaluationResult result = resultado();

        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(actor);
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any(AiIdeaEvaluationContext.class))).thenReturn(result);
        when(ideiaService.atualizarPrioridadeComPontuacaoAi(
                ideia,
                result.overallScore()
        )).thenReturn(new BigDecimal("7.34"));

        AiIdeaEvaluationResponse response = service.avaliarIdeia(42L);

        ArgumentCaptor<AiIdeaEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(AiIdeaEvaluationContext.class);
        verify(client).evaluate(contextCaptor.capture());
        AiIdeaEvaluationContext context = contextCaptor.getValue();
        ArgumentCaptor<AiIdeaEvaluation> evaluationCaptor =
                ArgumentCaptor.forClass(AiIdeaEvaluation.class);
        verify(evaluationRepository).save(evaluationCaptor.capture());
        AiIdeaEvaluation persisted = evaluationCaptor.getValue();

        assertAll(
                () -> assertEquals(42L, response.ideaId()),
                () -> assertEquals(9, response.strategicAlignment()),
                () -> assertEquals(8, response.impact()),
                () -> assertEquals(7, response.feasibility()),
                () -> assertEquals(8, response.innovation()),
                () -> assertEquals(
                        0,
                        new BigDecimal("8.1").compareTo(response.overallScore())
                ),
                () -> assertEquals("Resumo validado.", response.summary()),
                () -> assertEquals("Roteirização inteligente", context.title()),
                () -> assertEquals("Otimizar rotas de entrega", context.description()),
                () -> assertEquals("Rotas ineficientes", context.problem()),
                () -> assertEquals("Usar previsão de demanda", context.proposedSolution()),
                () -> assertEquals("Logística", context.category()),
                () -> assertEquals(
                        "Eficiência operacional — Reduzir custos e emissões",
                        context.strategicOrientation()
                ),
                () -> assertEquals(8, context.managerImpactScore()),
                () -> assertEquals(7, context.managerViabilityScore()),
                () -> assertEquals(42L, persisted.ideaId()),
                () -> assertEquals(17L, persisted.requestedByUserId()),
                () -> assertEquals("gemini", persisted.provider()),
                () -> assertEquals("gemini-3.6-flash", persisted.model()),
                () -> assertEquals(9, persisted.strategicAlignment()),
                () -> assertEquals(8, persisted.impact()),
                () -> assertEquals(7, persisted.feasibility()),
                () -> assertEquals(8, persisted.innovation()),
                () -> assertEquals(
                        0,
                        new BigDecimal("8.1").compareTo(
                                persisted.overallScore()
                        )
                ),
                () -> assertEquals("Resumo validado.", persisted.summary()),
                () -> assertEquals(36, persisted.id().length()),
                () -> assertNotNull(persisted.createdAt()),
                () -> assertSame(originalStatus, ideia.getStatusIdeia())
        );

        InOrder persistenceOrder = inOrder(
                client,
                evaluationRepository,
                ideiaService,
                auditService
        );
        persistenceOrder.verify(auditService).registrarEvento(
                eq(AuditEventType.AI_EVALUATION_REQUESTED),
                eq(17L),
                eq(AuditResourceType.AI_EVALUATION),
                eq("42"),
                any()
        );
        persistenceOrder.verify(client).evaluate(any());
        persistenceOrder.verify(evaluationRepository).save(any());
        persistenceOrder.verify(
                ideiaService
        ).atualizarPrioridadeComPontuacaoAi(
                ideia,
                result.overallScore()
        );
        persistenceOrder.verify(auditService).registrarEvento(
                eq(AuditEventType.AI_EVALUATION_COMPLETED),
                eq(17L),
                eq(AuditResourceType.AI_EVALUATION),
                eq("42"),
                any()
        );

        verifyAudit(
                AuditEventType.AI_EVALUATION_REQUESTED,
                actor.getId(),
                metadata -> "gemini".equals(metadata.get("provider"))
                        && "gemini-3.6-flash".equals(metadata.get("model"))
                        && !metadata.containsKey("overallScore")
        );
        verifyAudit(
                AuditEventType.AI_EVALUATION_COMPLETED,
                actor.getId(),
                metadata -> new BigDecimal("8.1").equals(
                        metadata.get("overallScore")
                ) && new BigDecimal("7.34").equals(
                        metadata.get("priorityScore")
                )
        );
        verify(
                auditService,
                never()
        ).registrarEvento(
                eq(AuditEventType.AI_EVALUATION_FAILED),
                any(),
                any(),
                any(),
                any()
        );
        verify(ideiaService).buscarEntidadePorId(42L);
        verifyNoMoreInteractions(ideiaService);
    }

    @Test
    void shouldReturnControlledFailureAndAuditWhenAiIsDisabled() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(null);

        AiEvaluationUnavailableException exception = assertThrows(
                AiEvaluationUnavailableException.class,
                () -> service.avaliarIdeia(42L)
        );

        assertEquals(
                "A avaliação por IA não está habilitada.",
                exception.getMessage()
        );
        assertSame(originalStatus, ideia.getStatusIdeia());
        verifyAudit(
                AuditEventType.AI_EVALUATION_FAILED,
                17L,
                metadata -> "AI_DISABLED".equals(metadata.get("failureType"))
        );
        verifyNoInteractions(client);
        verifyNoInteractions(evaluationRepository);
        verify(
                ideiaService,
                never()
        ).atualizarPrioridadeComPontuacaoAi(any(), any());
        verify(ideiaService).buscarEntidadePorId(42L);
        verifyNoMoreInteractions(ideiaService);
    }

    @Test
    void shouldPropagateClassifiedProviderFailureAndAuditIt() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        AiProviderException providerException = new AiProviderException(
                AiProviderFailure.TIMEOUT,
                "O provedor de IA excedeu o tempo limite."
        );
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenThrow(providerException);

        AiProviderException thrown = assertThrows(
                AiProviderException.class,
                () -> service.avaliarIdeia(42L)
        );

        assertSame(providerException, thrown);
        assertSame(originalStatus, ideia.getStatusIdeia());
        verifyAudit(
                AuditEventType.AI_EVALUATION_FAILED,
                17L,
                metadata -> "TIMEOUT".equals(metadata.get("failureType"))
        );
        verify(
                auditService,
                never()
        ).registrarEvento(
                eq(AuditEventType.AI_EVALUATION_COMPLETED),
                any(),
                any(),
                any(),
                any()
        );
        verifyNoInteractions(evaluationRepository);
        verify(
                ideiaService,
                never()
        ).atualizarPrioridadeComPontuacaoAi(any(), any());
        verify(ideiaService).buscarEntidadePorId(42L);
        verifyNoMoreInteractions(ideiaService);
    }

    @Test
    void shouldLogProviderFailureContextWithoutProviderMessageOrSecrets() {
        Ideia ideia = ideia();
        AiProviderException providerException = new AiProviderException(
                AiProviderFailure.TIMEOUT,
                "x-goog-api-key=never-log-this-key"
        );
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenThrow(providerException);

        Logger logger = (Logger) LoggerFactory.getLogger(
                AiIdeaEvaluationService.class
        );
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThrows(
                    AiProviderException.class,
                    () -> service.avaliarIdeia(42L)
            );
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        String logs = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .reduce("", (left, right) -> left + "\n" + right);

        assertTrue(logs.contains(
                "Falha na avaliação de IA: ideaId=42, provider=gemini, "
                        + "failureType=TIMEOUT"
        ));
        assertFalse(logs.contains("never-log-this-key"));
        assertFalse(logs.contains("x-goog-api-key"));
    }

    @Test
    void shouldNotRequestAiOrAuditWhenIdeaDoesNotExist() {
        ResourceNotFoundException notFound =
                new ResourceNotFoundException("Ideia não encontrada.");
        when(ideiaService.buscarEntidadePorId(404L)).thenThrow(notFound);

        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> service.avaliarIdeia(404L)
        );

        assertSame(notFound, thrown);
        verifyNoInteractions(
                usuarioAutenticadoService,
                clientProvider,
                client,
                evaluationRepository,
                auditService
        );
        verify(ideiaService).buscarEntidadePorId(404L);
        verifyNoMoreInteractions(ideiaService);
    }

    @Test
    void shouldUseOnlyOrientationTitleWhenItsDescriptionIsBlank() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        AiIdeaEvaluationResult result = resultado();
        ideia.getOrientacaoEstrategica().setDescricao("  ");
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenReturn(result);

        service.avaliarIdeia(42L);

        verify(client).evaluate(argThat(context ->
                "Eficiência operacional".equals(
                        context.strategicOrientation()
                )
        ));
        assertSame(originalStatus, ideia.getStatusIdeia());
        verify(ideiaService).buscarEntidadePorId(42L);
        verify(ideiaService).atualizarPrioridadeComPontuacaoAi(
                ideia,
                result.overallScore()
        );
        verifyNoMoreInteractions(ideiaService);
    }

    @Test
    void shouldFailSafelyAndAuditWhenEvaluationHistoryCannotBeStored() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenReturn(resultado());
        when(evaluationRepository.save(any())).thenThrow(
                new DataAccessResourceFailureException("mongo unavailable")
        );

        AiEvaluationPersistenceException exception = assertThrows(
                AiEvaluationPersistenceException.class,
                () -> service.avaliarIdeia(42L)
        );

        assertAll(
                () -> assertEquals(
                        "Não foi possível armazenar o histórico da avaliação de IA.",
                        exception.getMessage()
                ),
                () -> assertSame(originalStatus, ideia.getStatusIdeia())
        );
        verifyAudit(
                AuditEventType.AI_EVALUATION_FAILED,
                17L,
                metadata -> "PERSISTENCE_UNAVAILABLE".equals(
                        metadata.get("failureType")
                )
        );
        verify(
                auditService,
                never()
        ).registrarEvento(
                eq(AuditEventType.AI_EVALUATION_COMPLETED),
                any(),
                any(),
                any(),
                any()
        );
        verify(
                ideiaService,
                never()
        ).atualizarPrioridadeComPontuacaoAi(any(), any());
        verify(ideiaService).buscarEntidadePorId(42L);
        verifyNoMoreInteractions(ideiaService);
    }

    @Test
    void shouldFailSafelyWhenCalculatedPriorityCannotBePersisted() {
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        AiIdeaEvaluationResult result = resultado();
        PriorityCalculationUnavailableException persistenceFailure =
                new PriorityCalculationUnavailableException(
                        "Não foi possível persistir a prioridade da ideia.",
                        new DataAccessResourceFailureException(
                                "mysql unavailable"
                        )
                );

        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenReturn(result);
        when(ideiaService.atualizarPrioridadeComPontuacaoAi(
                ideia,
                result.overallScore()
        )).thenThrow(persistenceFailure);

        PriorityCalculationUnavailableException thrown = assertThrows(
                PriorityCalculationUnavailableException.class,
                () -> service.avaliarIdeia(42L)
        );

        assertSame(persistenceFailure, thrown);
        assertSame(originalStatus, ideia.getStatusIdeia());
        verify(evaluationRepository).save(any(AiIdeaEvaluation.class));
        verifyAudit(
                AuditEventType.AI_EVALUATION_FAILED,
                17L,
                metadata -> "PRIORITY_UNAVAILABLE".equals(
                        metadata.get("failureType")
                )
        );
        verify(
                auditService,
                never()
        ).registrarEvento(
                eq(AuditEventType.AI_EVALUATION_COMPLETED),
                any(),
                any(),
                any(),
                any()
        );
        verify(ideiaService).buscarEntidadePorId(42L);
        verify(ideiaService).atualizarPrioridadeComPontuacaoAi(
                ideia,
                result.overallScore()
        );
        verifyNoMoreInteractions(ideiaService);
    }

    private Ideia ideia() {
        Ideia ideia = new Ideia();
        ideia.setId(42L);
        ideia.setTitulo("Roteirização inteligente");
        ideia.setDescricao("Otimizar rotas de entrega");
        ideia.setProblema("Rotas ineficientes");
        ideia.setSolucaoProposta("Usar previsão de demanda");
        ideia.setImpacto(8);
        ideia.setViabilidade(7);
        ideia.setCategoria(new Categoria(3L, "Logística", "Operações"));

        OrientacaoEstrategica orientacao = new OrientacaoEstrategica();
        orientacao.setId(5L);
        orientacao.setTitulo("Eficiência operacional");
        orientacao.setDescricao("Reduzir custos e emissões");
        ideia.setOrientacaoEstrategica(orientacao);
        ideia.setStatusIdeia(new StatusIdeia());
        return ideia;
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private AiIdeaEvaluationResult resultado() {
        return new AiIdeaEvaluationResult(
                9,
                8,
                7,
                8,
                new BigDecimal("8.1"),
                "Resumo validado."
        );
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

    private void verifyAudit(
            AuditEventType eventType,
            Long actorId,
            java.util.function.Predicate<Map<String, Object>> metadataMatcher
    ) {
        verify(auditService).registrarEvento(
                eq(eventType),
                eq(actorId),
                eq(AuditResourceType.AI_EVALUATION),
                eq("42"),
                argThat(metadataMatcher::test)
        );
    }
}
