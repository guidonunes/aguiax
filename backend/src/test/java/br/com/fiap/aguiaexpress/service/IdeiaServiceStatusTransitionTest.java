package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.ideia.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.ideia.MostrarIdeiaDTO;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.model.Categoria;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.IdeiaRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IdeiaServiceStatusTransitionTest {

    private AuditService auditService;
    private IdeiaRepository ideiaRepository;
    private StatusIdeiaService statusIdeiaService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private IdeiaService ideiaService;

    @BeforeEach
    void setUp() {
        auditService = mock(AuditService.class);
        ideiaRepository = mock(IdeiaRepository.class);
        statusIdeiaService = mock(StatusIdeiaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);

        ideiaService = new IdeiaService(
                auditService,
                ideiaRepository,
                mock(UsuarioRepository.class),
                mock(CategoriaService.class),
                statusIdeiaService,
                mock(OrientacaoEstrategicaService.class),
                usuarioAutenticadoService,
                mock(IdeaPriorityService.class)
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allowedManagerTransitions")
    void shouldPersistAndAuditAllowedManagerTransition(
            NomeStatusIdeia currentStatus,
            NomeStatusIdeia newStatus,
            AuditEventType expectedEventType
    ) {
        Ideia ideia = idea(currentStatus);
        StatusIdeia targetStatus = status(9L, newStatus);

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(statusIdeiaService.buscarEntidadePorStatus(newStatus)).thenReturn(targetStatus);
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        MostrarIdeiaDTO result = ideiaService.atualizarStatusIdeia(
                42L,
                new AtualizarStatusIdeiaDTO(newStatus)
        );

        assertEquals(newStatus, result.statusIdeia());
        assertSame(targetStatus, ideia.getStatusIdeia());
        verify(ideiaRepository).save(ideia);
        verify(auditService).registrarEvento(
                expectedEventType,
                17L,
                AuditResourceType.IDEA,
                "42",
                Map.of(
                        "previousStatus", currentStatus.name(),
                        "newStatus", newStatus.name()
                )
        );
    }

    @ParameterizedTest(name = "{0} -/-> {1}")
    @MethodSource("forbiddenManagerTransitions")
    void shouldRejectForbiddenTransitionWithoutMutation(
            NomeStatusIdeia currentStatus,
            NomeStatusIdeia newStatus
    ) {
        Ideia ideia = idea(currentStatus);
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> ideiaService.atualizarStatusIdeia(
                        42L,
                        new AtualizarStatusIdeiaDTO(newStatus)
                )
        );

        assertEquals(
                "Transição de status não permitida: %s -> %s."
                        .formatted(currentStatus, newStatus),
                exception.getMessage()
        );
        assertSame(originalStatus, ideia.getStatusIdeia());
        verify(ideiaRepository, never()).save(ideia);
        verifyNoInteractions(
                statusIdeiaService,
                usuarioAutenticadoService,
                auditService
        );
    }

    @Test
    void shouldRejectDirectProjectConversionWithoutMutation() {
        Ideia ideia = idea(NomeStatusIdeia.APROVADA);
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> ideiaService.atualizarStatusIdeia(
                        42L,
                        new AtualizarStatusIdeiaDTO(
                                NomeStatusIdeia.CONVERTIDA_EM_PROJETO
                        )
                )
        );

        assertEquals(
                "A conversão da ideia deve ocorrer durante a criação do projeto.",
                exception.getMessage()
        );
        assertSame(originalStatus, ideia.getStatusIdeia());
        verify(ideiaRepository, never()).save(ideia);
        verifyNoInteractions(
                statusIdeiaService,
                usuarioAutenticadoService,
                auditService
        );
    }

    @Test
    void shouldRejectNullRequestBeforeRepositoryAccess() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> ideiaService.atualizarStatusIdeia(42L, null)
        );

        assertEquals(
                "O novo status da ideia é obrigatório.",
                exception.getMessage()
        );
        verifyNoInteractions(
                ideiaRepository,
                statusIdeiaService,
                usuarioAutenticadoService,
                auditService
        );
    }

    @Test
    void shouldRejectNullStatusBeforeRepositoryAccess() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> ideiaService.atualizarStatusIdeia(
                        42L,
                        new AtualizarStatusIdeiaDTO(null)
                )
        );

        assertEquals(
                "O novo status da ideia é obrigatório.",
                exception.getMessage()
        );
        verifyNoInteractions(
                ideiaRepository,
                statusIdeiaService,
                usuarioAutenticadoService,
                auditService
        );
    }

    private static Stream<Arguments> allowedManagerTransitions() {
        return Stream.of(
                Arguments.of(
                        NomeStatusIdeia.NOVA,
                        NomeStatusIdeia.EM_ANALISE,
                        AuditEventType.IDEA_UPDATED
                ),
                Arguments.of(
                        NomeStatusIdeia.EM_ANALISE,
                        NomeStatusIdeia.PRIORIZADA,
                        AuditEventType.IDEA_PRIORITIZED
                ),
                Arguments.of(
                        NomeStatusIdeia.EM_ANALISE,
                        NomeStatusIdeia.APROVADA,
                        AuditEventType.IDEA_APPROVED
                ),
                Arguments.of(
                        NomeStatusIdeia.EM_ANALISE,
                        NomeStatusIdeia.REJEITADA,
                        AuditEventType.IDEA_REJECTED
                ),
                Arguments.of(
                        NomeStatusIdeia.PRIORIZADA,
                        NomeStatusIdeia.APROVADA,
                        AuditEventType.IDEA_APPROVED
                ),
                Arguments.of(
                        NomeStatusIdeia.PRIORIZADA,
                        NomeStatusIdeia.REJEITADA,
                        AuditEventType.IDEA_REJECTED
                )
        );
    }

    private static Stream<Arguments> forbiddenManagerTransitions() {
        return Stream.of(
                Arguments.of(NomeStatusIdeia.NOVA, NomeStatusIdeia.APROVADA),
                Arguments.of(NomeStatusIdeia.EM_ANALISE, NomeStatusIdeia.NOVA),
                Arguments.of(NomeStatusIdeia.PRIORIZADA, NomeStatusIdeia.EM_ANALISE),
                Arguments.of(NomeStatusIdeia.APROVADA, NomeStatusIdeia.REJEITADA),
                Arguments.of(NomeStatusIdeia.REJEITADA, NomeStatusIdeia.EM_ANALISE),
                Arguments.of(
                        NomeStatusIdeia.CONVERTIDA_EM_PROJETO,
                        NomeStatusIdeia.APROVADA
                ),
                Arguments.of(NomeStatusIdeia.NOVA, NomeStatusIdeia.NOVA)
        );
    }

    private Ideia idea(NomeStatusIdeia currentStatus) {
        OrientacaoEstrategica orientation = new OrientacaoEstrategica();
        orientation.setId(4L);
        orientation.setTitulo("Orientação");
        orientation.setAreaEstrategica("Operações");

        Ideia ideia = new Ideia();
        ideia.setId(42L);
        ideia.setUsuario(user(7L));
        ideia.setCategoria(new Categoria(3L, "Tecnologia", "Descrição"));
        ideia.setOrientacaoEstrategica(orientation);
        ideia.setStatusIdeia(status(2L, currentStatus));
        ideia.setTitulo("Ideia");
        ideia.setDescricao("Descrição");
        ideia.setImpacto(8);
        ideia.setViabilidade(7);
        ideia.setPrioridade(new BigDecimal("7.00"));
        ideia.setTotalVotos(5);
        return ideia;
    }

    private StatusIdeia status(Long id, NomeStatusIdeia status) {
        return new StatusIdeia(id, status, status.name());
    }

    private Usuario user(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário");
        return usuario;
    }
}
