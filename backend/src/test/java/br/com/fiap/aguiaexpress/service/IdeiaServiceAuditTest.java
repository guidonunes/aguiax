package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.ideia.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.ideia.RegistrarIdeiaDTO;
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
import org.mockito.InOrder;

import java.util.Map;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        auditService = mock(AuditService.class);
        ideiaRepository = mock(IdeiaRepository.class);
        statusIdeiaService = mock(StatusIdeiaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        categoriaService = mock(CategoriaService.class);
        orientacaoService = mock(OrientacaoEstrategicaService.class);

        ideiaService = new IdeiaService(
                auditService,
                ideiaRepository,
                mock(UsuarioRepository.class),
                categoriaService,
                statusIdeiaService,
                orientacaoService,
                usuarioAutenticadoService,
                mock(IdeaPriorityService.class)
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

        ideiaService.criarIdeia(new RegistrarIdeiaDTO(
                3L,
                4L,
                "Ideia",
                "Descrição",
                "Problema",
                "Solução"
        ));

        InOrder order = inOrder(ideiaRepository, auditService);
        order.verify(ideiaRepository).save(any(Ideia.class));
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

        verifyNoInteractions(auditService);
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
