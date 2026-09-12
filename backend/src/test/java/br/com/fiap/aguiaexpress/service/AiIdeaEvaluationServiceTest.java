package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.ai.client.AiIdeaEvaluationClient;
import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.ai.client.AiProviderFailure;
import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.config.ai.AiProviderProperties;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationContext;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResponse;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResult;
import br.com.fiap.aguiaexpress.exception.AiEvaluationUnavailableException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.Categoria;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AiIdeaEvaluationServiceTest {

    private IdeiaService ideiaService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private ObjectProvider<AiIdeaEvaluationClient> clientProvider;
    private AiIdeaEvaluationClient client;
    private AuditService auditService;
    private AiIdeaEvaluationService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ideiaService = mock(IdeiaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        clientProvider = mock(ObjectProvider.class);
        client = mock(AiIdeaEvaluationClient.class);
        auditService = mock(AuditService.class);

        service = new AiIdeaEvaluationService(
                ideiaService,
                usuarioAutenticadoService,
                clientProvider,
                providerProperties(),
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

        AiIdeaEvaluationResponse response = service.avaliarIdeia(42L);

        ArgumentCaptor<AiIdeaEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(AiIdeaEvaluationContext.class);
        verify(client).evaluate(contextCaptor.capture());
        AiIdeaEvaluationContext context = contextCaptor.getValue();

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
                () -> assertSame(originalStatus, ideia.getStatusIdeia())
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
    }

    @Test
    void shouldReturnControlledFailureAndAuditWhenAiIsDisabled() {
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia());
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
        verifyAudit(
                AuditEventType.AI_EVALUATION_FAILED,
                17L,
                metadata -> "AI_DISABLED".equals(metadata.get("failureType"))
        );
        verifyNoInteractions(client);
    }

    @Test
    void shouldPropagateClassifiedProviderFailureAndAuditIt() {
        AiProviderException providerException = new AiProviderException(
                AiProviderFailure.TIMEOUT,
                "O provedor de IA excedeu o tempo limite."
        );
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia());
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenThrow(providerException);

        AiProviderException thrown = assertThrows(
                AiProviderException.class,
                () -> service.avaliarIdeia(42L)
        );

        assertSame(providerException, thrown);
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
                auditService
        );
    }

    @Test
    void shouldUseOnlyOrientationTitleWhenItsDescriptionIsBlank() {
        Ideia ideia = ideia();
        ideia.getOrientacaoEstrategica().setDescricao("  ");
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(usuario(17L));
        when(clientProvider.getIfAvailable()).thenReturn(client);
        when(client.evaluate(any())).thenReturn(resultado());

        service.avaliarIdeia(42L);

        verify(client).evaluate(argThat(context ->
                "Eficiência operacional".equals(
                        context.strategicOrientation()
                )
        ));
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
