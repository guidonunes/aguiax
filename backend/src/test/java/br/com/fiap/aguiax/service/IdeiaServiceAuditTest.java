package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.audit.model.AuditEventType;
import br.com.fiap.aguiax.audit.model.AuditResourceType;
import br.com.fiap.aguiax.audit.service.AuditService;
import br.com.fiap.aguiax.config.observability.BusinessMetrics;
import br.com.fiap.aguiax.dto.ideia.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.MostrarIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.RegistrarIdeiaDTO;
import br.com.fiap.aguiax.exception.DuplicateResourceException;
import br.com.fiap.aguiax.model.Categoria;
import br.com.fiap.aguiax.model.Ideia;
import br.com.fiap.aguiax.model.NomeStatusIdeia;
import br.com.fiap.aguiax.model.OrientacaoEstrategica;
import br.com.fiap.aguiax.model.StatusIdeia;
import br.com.fiap.aguiax.model.Usuario;
import br.com.fiap.aguiax.repository.IdeiaRepository;
import br.com.fiap.aguiax.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IdeiaServiceAuditTest {

    private AuditService auditService;
    private IdeiaRepository ideiaRepository;
    private StatusIdeiaService statusIdeiaService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private CategoriaService categoriaService;
    private OrientacaoEstrategicaService orientacaoService;
    private IdeiaService ideiaService;
    private MilestoneBadgeService milestoneBadgeService;

    @BeforeEach
    void setUp() {
        auditService = mock(AuditService.class);
        ideiaRepository = mock(IdeiaRepository.class);
        statusIdeiaService = mock(StatusIdeiaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        categoriaService = mock(CategoriaService.class);
        orientacaoService = mock(OrientacaoEstrategicaService.class);
        milestoneBadgeService = mock(MilestoneBadgeService.class);

        ideiaService = new IdeiaService(
                auditService,
                ideiaRepository,
                mock(UsuarioRepository.class),
                categoriaService,
                statusIdeiaService,
                orientacaoService,
                usuarioAutenticadoService,
                mock(IdeaPriorityService.class),
                mock(BusinessMetrics.class),
                milestoneBadgeService
        );
    }

    @Test
    void shouldAuditIdeaCreationAfterRelationalSave() {
        Usuario actor = user(17L);
        Categoria categoria = new Categoria(3L, "Tecnologia", "Descrição");
        OrientacaoEstrategica orientacao = orientation(4L);
        StatusIdeia status = new StatusIdeia(1L, NomeStatusIdeia.NOVA, "Nova");

        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(actor);
        when(categoriaService.buscarEntidadePorId(3L)).thenReturn(categoria);
        when(orientacaoService.buscarEntidadePorId(4L)).thenReturn(orientacao);
        when(statusIdeiaService.buscarEntidadePorStatus(NomeStatusIdeia.NOVA))
                .thenReturn(status);
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(invocation -> {
            Ideia ideia = invocation.getArgument(0);
            ideia.setId(42L);
            return ideia;
        });

        MostrarIdeiaDTO response = ideiaService.criarIdeia(new RegistrarIdeiaDTO(
                3L,
                4L,
                "Ideia",
                "Descrição",
                "Problema",
                "Solução"
        ));

        assertAll(
                () -> assertEquals(42L, response.id()),
                () -> assertEquals(17L, response.usuarioId()),
                () -> assertEquals(3L, response.categoriaId()),
                () -> assertEquals(4L, response.orientacaoId()),
                () -> assertEquals(NomeStatusIdeia.NOVA, response.statusIdeia()),
                () -> assertEquals(0, response.impacto()),
                () -> assertEquals(0, response.viabilidade()),
                () -> assertEquals(0, response.totalVotos()),
                () -> assertEquals(new BigDecimal("0.00"), response.prioridade()),
                () -> assertNotNull(response.dataCriacao()),
                () -> assertNotNull(response.dataAtualizacao())
        );

        InOrder order = inOrder(ideiaRepository, milestoneBadgeService, auditService);
        order.verify(ideiaRepository).save(any(Ideia.class));
        order.verify(milestoneBadgeService).awardForIdea(any(Ideia.class));
        order.verify(auditService).registrarEvento(
                AuditEventType.IDEA_CREATED,
                17L,
                AuditResourceType.IDEA,
                "42",
                Map.of(
                        "status", "NOVA",
                        "categoryId", 3L,
                        "orientationId", 4L
                )
        );
    }

    @Test
    void shouldRejectDuplicateIdeaTitleBeforeLoadingRelationships() {
        when(ideiaRepository.existsByTitulo("Ideia duplicada")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> ideiaService.criarIdeia(new RegistrarIdeiaDTO(
                        3L,
                        4L,
                        "Ideia duplicada",
                        "Descrição",
                        "Problema",
                        "Solução"
                ))
        );

        assertEquals(
                "Já existe uma ideia cadastrada com esse título.",
                exception.getMessage()
        );
        verify(ideiaRepository).existsByTitulo("Ideia duplicada");
        verifyNoInteractions(
                usuarioAutenticadoService,
                categoriaService,
                orientacaoService,
                statusIdeiaService,
                auditService
        );
    }

    @Test
    void shouldNotAuditWhenIdeaSaveFails() {
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(categoriaService.buscarEntidadePorId(3L))
                .thenReturn(new Categoria(3L, "Tecnologia", "Descrição"));
        when(orientacaoService.buscarEntidadePorId(4L)).thenReturn(orientation(4L));
        when(statusIdeiaService.buscarEntidadePorStatus(NomeStatusIdeia.NOVA))
                .thenReturn(new StatusIdeia(1L, NomeStatusIdeia.NOVA, "Nova"));
        when(ideiaRepository.save(any(Ideia.class)))
                .thenThrow(new IllegalStateException("MySQL unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> ideiaService.criarIdeia(new RegistrarIdeiaDTO(
                        3L,
                        4L,
                        "Ideia",
                        "Descrição",
                        "Problema",
                        "Solução"
                ))
        );

        verifyNoInteractions(auditService, milestoneBadgeService);
    }

    @Test
    void shouldAuditIdeaUpdateAfterRelationalSave() {
        Ideia ideia = idea(42L, NomeStatusIdeia.NOVA);
        Categoria newCategory = new Categoria(8L, "Operações", "Descrição");
        OrientacaoEstrategica newOrientation = orientation(9L);
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(7L));
        when(categoriaService.buscarEntidadePorId(8L)).thenReturn(newCategory);
        when(orientacaoService.buscarEntidadePorId(9L))
                .thenReturn(newOrientation);
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        ideiaService.atualizarIdeia(42L, new RegistrarIdeiaDTO(
                8L,
                9L,
                "Ideia atualizada",
                "Descrição atualizada",
                "Problema atualizado",
                "Solução atualizada"
        ));

        InOrder order = inOrder(ideiaRepository, auditService);
        order.verify(ideiaRepository).save(ideia);
        order.verify(auditService).registrarEvento(
                AuditEventType.IDEA_UPDATED,
                7L,
                AuditResourceType.IDEA,
                "42",
                Map.of(
                        "categoryId", 8L,
                        "orientationId", 9L
                )
        );
    }

    @Test
    void shouldAuditIdeaDeletionAfterRelationalDelete() {
        Ideia ideia = idea(42L, NomeStatusIdeia.NOVA);
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(user(7L));

        ideiaService.deletarIdeia(42L);

        InOrder order = inOrder(ideiaRepository, auditService);
        order.verify(ideiaRepository).delete(ideia);
        order.verify(auditService).registrarEvento(
                AuditEventType.IDEA_DELETED,
                7L,
                AuditResourceType.IDEA,
                "42",
                Map.of("previousStatus", "NOVA")
        );
    }

    @Test
    void shouldAuditPrioritizedStatus() {
        verifyStatusAudit(NomeStatusIdeia.PRIORIZADA, AuditEventType.IDEA_PRIORITIZED);
    }

    @Test
    void shouldAuditApprovedStatus() {
        verifyStatusAudit(NomeStatusIdeia.APROVADA, AuditEventType.IDEA_APPROVED);
    }

    @Test
    void shouldAuditRejectedStatus() {
        verifyStatusAudit(NomeStatusIdeia.REJEITADA, AuditEventType.IDEA_REJECTED);
    }

    private void verifyStatusAudit(
            NomeStatusIdeia newStatus,
            AuditEventType expectedEvent
    ) {
        Ideia ideia = idea(42L, NomeStatusIdeia.EM_ANALISE);
        StatusIdeia status = new StatusIdeia(2L, newStatus, newStatus.name());

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(statusIdeiaService.buscarEntidadePorStatus(newStatus)).thenReturn(status);
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        ideiaService.atualizarStatusIdeia(42L, new AtualizarStatusIdeiaDTO(newStatus));

        verify(auditService).registrarEvento(
                expectedEvent,
                17L,
                AuditResourceType.IDEA,
                "42",
                Map.of(
                        "previousStatus", "EM_ANALISE",
                        "newStatus", newStatus.name()
                )
        );
    }

    private Ideia idea(Long id, NomeStatusIdeia status) {
        Ideia ideia = new Ideia();
        ideia.setId(id);
        ideia.setUsuario(user(7L));
        ideia.setCategoria(new Categoria(3L, "Tecnologia", "Descrição"));
        ideia.setOrientacaoEstrategica(orientation(4L));
        ideia.setStatusIdeia(new StatusIdeia(1L, status, status.name()));
        ideia.setTitulo("Ideia");
        ideia.setDescricao("Descrição");
        return ideia;
    }

    private Usuario user(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário");
        return usuario;
    }

    private OrientacaoEstrategica orientation(Long id) {
        OrientacaoEstrategica orientacao = new OrientacaoEstrategica();
        orientacao.setId(id);
        orientacao.setTitulo("Orientação");
        orientacao.setAreaEstrategica("Operações");
        return orientacao;
    }
}
