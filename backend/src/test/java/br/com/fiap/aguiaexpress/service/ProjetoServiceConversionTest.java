package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.config.observability.BusinessMetrics;
import br.com.fiap.aguiaexpress.dto.projeto.MostrarProjetoDTO;
import br.com.fiap.aguiaexpress.dto.projeto.RegistrarProjetoDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.Projeto;
import br.com.fiap.aguiaexpress.model.StatusIdeia;
import br.com.fiap.aguiaexpress.model.StatusProjeto;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.IdeiaRepository;
import br.com.fiap.aguiaexpress.repository.ProjetoRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProjetoServiceConversionTest {

    private UsuarioAutenticadoService usuarioAutenticadoService;
    private AuditService auditService;
    private ProjetoRepository projetoRepository;
    private UsuarioRepository usuarioRepository;
    private IdeiaRepository ideiaRepository;
    private StatusIdeiaService statusIdeiaService;
    private StatusProjetoService statusProjetoService;
    private BusinessMetrics businessMetrics;
    private ProjetoService projetoService;

    @BeforeEach
    void setUp() {
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        auditService = mock(AuditService.class);
        projetoRepository = mock(ProjetoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        ideiaRepository = mock(IdeiaRepository.class);
        statusIdeiaService = mock(StatusIdeiaService.class);
        statusProjetoService = mock(StatusProjetoService.class);
        businessMetrics = mock(BusinessMetrics.class);

        IdeiaService ideiaService = new IdeiaService(
                auditService,
                ideiaRepository,
                usuarioRepository,
                mock(CategoriaService.class),
                statusIdeiaService,
                mock(OrientacaoEstrategicaService.class),
                usuarioAutenticadoService,
                mock(IdeaPriorityService.class),
                mock(BusinessMetrics.class)
        );

        projetoService = new ProjetoService(
                usuarioAutenticadoService,
                auditService,
                projetoRepository,
                usuarioRepository,
                ideiaService,
                statusProjetoService,
                businessMetrics
        );
    }

    @Test
    void shouldCreateProjectAndConvertApprovedIdea() {
        Ideia ideia = idea(NomeStatusIdeia.APROVADA);
        Usuario actor = manager(17L);
        Usuario responsible = manager(7L);
        StatusProjeto planned = new StatusProjeto(
                1L,
                NomeStatusProjeto.PLANEJADO,
                "Planejado"
        );
        StatusIdeia converted = new StatusIdeia(
                6L,
                NomeStatusIdeia.CONVERTIDA_EM_PROJETO,
                "Convertida em projeto"
        );

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(actor);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(responsible));
        when(statusProjetoService.buscarEntidadePorStatus(NomeStatusProjeto.PLANEJADO))
                .thenReturn(planned);
        when(statusIdeiaService.buscarEntidadePorStatus(
                NomeStatusIdeia.CONVERTIDA_EM_PROJETO
        )).thenReturn(converted);
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto projeto = invocation.getArgument(0);
            projeto.setId(99L);
            return projeto;
        });
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        MostrarProjetoDTO result = projetoService.criarProjeto(request());

        assertEquals(99L, result.id());
        assertEquals(42L, result.ideiaId());
        assertEquals(NomeStatusIdeia.CONVERTIDA_EM_PROJETO,
                ideia.getStatusIdeia().getStatus());
        assertNotNull(ideia.getDataAtualizacao());

        InOrder order = inOrder(
                projetoRepository,
                ideiaRepository,
                auditService,
                businessMetrics
        );
        order.verify(projetoRepository).save(any(Projeto.class));
        order.verify(ideiaRepository).save(ideia);
        order.verify(projetoRepository).flush();
        order.verify(auditService).registrarEvento(
                AuditEventType.PROJECT_CREATED,
                17L,
                AuditResourceType.PROJECT,
                "99",
                Map.of(
                        "ideaId", 42L,
                        "responsibleUserId", 7L,
                        "status", "PLANEJADO",
                        "previousIdeaStatus", "APROVADA",
                        "newIdeaStatus", "CONVERTIDA_EM_PROJETO"
                )
        );
        order.verify(businessMetrics).recordProjectCreated();
    }

    @ParameterizedTest
    @EnumSource(
            value = NomeStatusIdeia.class,
            names = "APROVADA",
            mode = EnumSource.Mode.EXCLUDE
    )
    void shouldRejectEveryIneligibleIdeaStatus(NomeStatusIdeia ideaStatus) {
        Ideia ideia = idea(ideaStatus);
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> projetoService.criarProjeto(request())
        );

        assertEquals(
                "Transição de status não permitida: %s -> CONVERTIDA_EM_PROJETO."
                        .formatted(ideaStatus),
                exception.getMessage()
        );
        verify(projetoRepository, never()).save(any());
        verify(ideiaRepository, never()).save(any());
        verifyNoInteractions(
                usuarioAutenticadoService,
                usuarioRepository,
                statusProjetoService,
                statusIdeiaService,
                auditService,
                businessMetrics
        );
    }

    @Test
    void shouldRejectSecondProjectBeforeAnyMutation() {
        Ideia ideia = idea(NomeStatusIdeia.APROVADA);
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(projetoRepository.existsByIdeia(ideia)).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> projetoService.criarProjeto(request())
        );

        assertEquals(
                "Essa ideia já foi convertida em projeto.",
                exception.getMessage()
        );
        verify(projetoRepository, never()).save(any());
        verify(ideiaRepository, never()).save(any());
        verifyNoInteractions(
                usuarioAutenticadoService,
                usuarioRepository,
                statusProjetoService,
                statusIdeiaService,
                auditService,
                businessMetrics
        );
    }

    @Test
    void shouldNotConvertOrAuditWhenProjectSaveFails() {
        Ideia ideia = prepareApprovedIdeaDependencies();
        when(projetoRepository.save(any(Projeto.class)))
                .thenThrow(new IllegalStateException("Project insert failed"));

        assertThrows(
                IllegalStateException.class,
                () -> projetoService.criarProjeto(request())
        );

        assertEquals(NomeStatusIdeia.APROVADA, ideia.getStatusIdeia().getStatus());
        verify(ideiaRepository, never()).save(any());
        verifyNoInteractions(auditService, businessMetrics);
    }

    @Test
    void shouldNotAuditWhenIdeaConversionSaveFails() {
        Ideia ideia = prepareApprovedIdeaDependencies();
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto projeto = invocation.getArgument(0);
            projeto.setId(99L);
            return projeto;
        });
        when(ideiaRepository.save(ideia))
                .thenThrow(new IllegalStateException("Idea update failed"));

        assertThrows(
                IllegalStateException.class,
                () -> projetoService.criarProjeto(request())
        );

        verify(projetoRepository).save(any(Projeto.class));
        verify(ideiaRepository).save(ideia);
        verifyNoInteractions(auditService, businessMetrics);
    }

    @Test
    void shouldNotAuditWhenRelationalFlushFails() {
        Ideia ideia = prepareApprovedIdeaDependencies();
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto projeto = invocation.getArgument(0);
            projeto.setId(99L);
            return projeto;
        });
        when(ideiaRepository.save(ideia)).thenReturn(ideia);
        doThrow(new IllegalStateException("MySQL constraint failure"))
                .when(projetoRepository).flush();

        assertThrows(
                IllegalStateException.class,
                () -> projetoService.criarProjeto(request())
        );

        verify(projetoRepository).save(any(Projeto.class));
        verify(ideiaRepository).save(ideia);
        verify(projetoRepository).flush();
        verifyNoInteractions(auditService, businessMetrics);
    }

    @Test
    void projectCreationDeclaresRelationalTransactionBoundary() throws Exception {
        Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(
                ProjetoService.class.getDeclaredMethod(
                        "criarProjeto",
                        RegistrarProjetoDTO.class
                ),
                Transactional.class
        );

        assertNotNull(transactional);
        assertTrue(transactional.rollbackFor().length == 0,
                "Default rollback must include runtime persistence failures");
    }

    private Ideia prepareApprovedIdeaDependencies() {
        Ideia ideia = idea(NomeStatusIdeia.APROVADA);
        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(manager(17L));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(manager(7L)));
        when(statusProjetoService.buscarEntidadePorStatus(NomeStatusProjeto.PLANEJADO))
                .thenReturn(new StatusProjeto(
                        1L,
                        NomeStatusProjeto.PLANEJADO,
                        "Planejado"
                ));
        when(statusIdeiaService.buscarEntidadePorStatus(
                NomeStatusIdeia.CONVERTIDA_EM_PROJETO
        )).thenReturn(new StatusIdeia(
                6L,
                NomeStatusIdeia.CONVERTIDA_EM_PROJETO,
                "Convertida em projeto"
        ));
        return ideia;
    }

    private RegistrarProjetoDTO request() {
        return new RegistrarProjetoDTO(
                42L,
                7L,
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

    private Ideia idea(NomeStatusIdeia status) {
        Ideia ideia = new Ideia();
        ideia.setId(42L);
        ideia.setTitulo("Ideia aprovada");
        ideia.setStatusIdeia(new StatusIdeia(4L, status, status.name()));
        return ideia;
    }

    private Usuario manager(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Gestor " + id);
        usuario.setTipoPerfil(new TipoPerfil(2L, NomePerfil.GESTOR, "Gestor"));
        return usuario;
    }
}
