package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.AtualizarStatusProjetoDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarProjetoDTO;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.Projeto;
import br.com.fiap.aguiaexpress.model.StatusProjeto;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.ProjetoRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProjetoServiceAuditTest {

    private UsuarioAutenticadoService usuarioAutenticadoService;
    private AuditService auditService;
    private ProjetoRepository projetoRepository;
    private UsuarioRepository usuarioRepository;
    private IdeiaService ideiaService;
    private StatusProjetoService statusProjetoService;
    private ProjetoService projetoService;

    @BeforeEach
    void setUp() {
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        auditService = mock(AuditService.class);
        projetoRepository = mock(ProjetoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        ideiaService = mock(IdeiaService.class);
        statusProjetoService = mock(StatusProjetoService.class);

        projetoService = new ProjetoService(
                usuarioAutenticadoService,
                auditService,
                projetoRepository,
                usuarioRepository,
                ideiaService,
                statusProjetoService
        );
    }

    @Test
    void shouldAuditProjectCreationAfterRelationalSaveUsingAuthenticatedActor() {
        Usuario actor = user(17L);
        Usuario responsibleUser = user(7L);
        Ideia ideia = idea(42L);
        StatusProjeto status = status(NomeStatusProjeto.PLANEJADO);

        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(actor);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(responsibleUser));
        when(statusProjetoService.buscarEntidadePorStatus(NomeStatusProjeto.PLANEJADO))
                .thenReturn(status);
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto projeto = invocation.getArgument(0);
            projeto.setId(99L);
            return projeto;
        });

        projetoService.criarProjeto(projectRequest(42L, 7L));

        InOrder order = inOrder(projetoRepository, auditService);
        order.verify(projetoRepository).save(any(Projeto.class));
        order.verify(auditService).registrarEvento(
                AuditEventType.PROJECT_CREATED,
                17L,
                AuditResourceType.PROJECT,
                "99",
                Map.of(
                        "ideaId", 42L,
                        "responsibleUserId", 7L,
                        "status", "PLANEJADO"
                )
        );
    }

    @Test
    void shouldAuditProjectUpdateWithRelationshipChanges() {
        Projeto projeto = project(99L, 41L, 6L, NomeStatusProjeto.PLANEJADO);
        Ideia newIdea = idea(42L);
        Usuario newResponsibleUser = user(7L);

        when(projetoRepository.findById(99L)).thenReturn(Optional.of(projeto));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(newIdea);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(newResponsibleUser));
        when(projetoRepository.save(projeto)).thenReturn(projeto);

        projetoService.atualizarProjeto(99L, projectRequest(42L, 7L));

        verify(auditService).registrarEvento(
                AuditEventType.PROJECT_UPDATED,
                17L,
                AuditResourceType.PROJECT,
                "99",
                Map.of(
                        "previousIdeaId", 41L,
                        "newIdeaId", 42L,
                        "previousResponsibleUserId", 6L,
                        "newResponsibleUserId", 7L
                )
        );
    }

    @Test
    void shouldUseCompletedEventForCompletedProject() {
        Projeto projeto = project(99L, 42L, 7L, NomeStatusProjeto.EM_ANDAMENTO);
        StatusProjeto completed = status(NomeStatusProjeto.CONCLUIDO);

        when(projetoRepository.findById(99L)).thenReturn(Optional.of(projeto));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(statusProjetoService.buscarEntidadePorStatus(NomeStatusProjeto.CONCLUIDO))
                .thenReturn(completed);
        when(projetoRepository.save(projeto)).thenReturn(projeto);

        projetoService.atualizarStatusProjeto(
                99L,
                new AtualizarStatusProjetoDTO(NomeStatusProjeto.CONCLUIDO)
        );

        verify(auditService).registrarEvento(
                AuditEventType.PROJECT_COMPLETED,
                17L,
                AuditResourceType.PROJECT,
                "99",
                Map.of(
                        "previousStatus", "EM_ANDAMENTO",
                        "newStatus", "CONCLUIDO"
                )
        );
    }

    @Test
    void shouldNotAuditWhenProjectSaveFails() {
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(idea(42L));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(user(17L));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(statusProjetoService.buscarEntidadePorStatus(NomeStatusProjeto.PLANEJADO))
                .thenReturn(status(NomeStatusProjeto.PLANEJADO));
        when(projetoRepository.save(any(Projeto.class)))
                .thenThrow(new IllegalStateException("MySQL unavailable"));

        assertThrows(
                IllegalStateException.class,
                () -> projetoService.criarProjeto(projectRequest(42L, 7L))
        );

        verifyNoInteractions(auditService);
    }

    private RegistrarProjetoDTO projectRequest(Long ideaId, Long responsibleUserId) {
        return new RegistrarProjetoDTO(
                ideaId,
                responsibleUserId,
                "Projeto",
                "Descrição",
                "Objetivo",
                BigDecimal.TEN,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                null,
                null,
                null
        );
    }

    private Projeto project(
            Long id,
            Long ideaId,
            Long responsibleUserId,
            NomeStatusProjeto projectStatus
    ) {
        Projeto projeto = new Projeto();
        projeto.setId(id);
        projeto.setIdeia(idea(ideaId));
        projeto.setUsuario(user(responsibleUserId));
        projeto.setStatusProjeto(status(projectStatus));
        projeto.setTitulo("Projeto");
        projeto.setInvestimento(BigDecimal.ZERO);
        projeto.setRoi(BigDecimal.ZERO);
        projeto.setRetornoFinanceiro(BigDecimal.ZERO);
        projeto.setReducaoCustos(BigDecimal.ZERO);
        projeto.setGanhoProdutividade(BigDecimal.ZERO);
        return projeto;
    }

    private Ideia idea(Long id) {
        Ideia ideia = new Ideia();
        ideia.setId(id);
        ideia.setTitulo("Ideia");
        return ideia;
    }

    private Usuario user(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário " + id);
        usuario.setTipoPerfil(new TipoPerfil(2L, NomePerfil.GESTOR, "Gestor"));
        return usuario;
    }

    private StatusProjeto status(NomeStatusProjeto projectStatus) {
        return new StatusProjeto(1L, projectStatus, projectStatus.name());
    }
}
