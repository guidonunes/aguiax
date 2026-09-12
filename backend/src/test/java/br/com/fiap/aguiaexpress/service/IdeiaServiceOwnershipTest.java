package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.audit.model.AuditEventType;
import br.com.fiap.aguiaexpress.audit.model.AuditResourceType;
import br.com.fiap.aguiaexpress.audit.service.AuditService;
import br.com.fiap.aguiaexpress.dto.ideia.MostrarIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.ideia.RegistrarIdeiaDTO;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
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

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IdeiaServiceOwnershipTest {

    private IdeiaRepository ideiaRepository;
    private CategoriaService categoriaService;
    private OrientacaoEstrategicaService orientacaoService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private AuditService auditService;
    private IdeiaService ideiaService;

    @BeforeEach
    void setUp() {
        ideiaRepository = mock(IdeiaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        categoriaService = mock(CategoriaService.class);
        StatusIdeiaService statusIdeiaService = mock(StatusIdeiaService.class);
        orientacaoService = mock(OrientacaoEstrategicaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        auditService = mock(AuditService.class);

        ideiaService = new IdeiaService(
                auditService,
                ideiaRepository,
                usuarioRepository,
                categoriaService,
                statusIdeiaService,
                orientacaoService,
                usuarioAutenticadoService
        );
    }

    @Test
    void ownerCanUpdateWithoutTransferringIdeaOwnership() {
        Usuario persistedOwner = user(1L, "Original owner");
        Usuario authenticatedOwner = user(1L, "Authenticated instance");
        Ideia ideia = idea(42L, persistedOwner);
        Categoria newCategory = category(20L, "Tecnologia");
        OrientacaoEstrategica newOrientation = orientation(30L, "Inovacao");
        RegistrarIdeiaDTO request = updateRequest();

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(authenticatedOwner);
        when(categoriaService.buscarEntidadePorId(20L)).thenReturn(newCategory);
        when(orientacaoService.buscarEntidadePorId(30L)).thenReturn(newOrientation);
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        MostrarIdeiaDTO result = ideiaService.atualizarIdeia(42L, request);

        assertEquals("Novo titulo", result.titulo());
        assertEquals(1L, result.usuarioId());
        assertSame(
                persistedOwner,
                ideia.getUsuario(),
                "Updating an idea must preserve the original owner entity"
        );
        assertSame(newCategory, ideia.getCategoria());
        assertSame(newOrientation, ideia.getOrientacaoEstrategica());
        verify(ideiaRepository).save(ideia);
        verify(auditService).registrarEvento(
                AuditEventType.IDEA_UPDATED,
                1L,
                AuditResourceType.IDEA,
                "42",
                Map.of("categoryId", 20L, "orientationId", 30L)
        );
    }

    @Test
    void nonOwnerCannotUpdateAndNoMutationDependencyIsCalled() {
        Usuario owner = user(1L, "Owner");
        Usuario nonOwner = user(2L, "Non-owner");
        Ideia ideia = idea(42L, owner);

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(nonOwner);
        stubUpdateDependencies(ideia);

        assertThrows(
                UnauthorizedActionException.class,
                () -> ideiaService.atualizarIdeia(42L, updateRequest())
        );

        assertSame(owner, ideia.getUsuario());
        verify(ideiaRepository, never()).save(any());
        verifyNoInteractions(categoriaService, orientacaoService);
        verifyNoInteractions(auditService);
    }

    @Test
    void ownerCanDeleteIdea() {
        Usuario persistedOwner = user(1L, "Original owner");
        Usuario authenticatedOwner = user(1L, "Authenticated instance");
        Ideia ideia = idea(42L, persistedOwner);

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(authenticatedOwner);

        ideiaService.deletarIdeia(42L);

        verify(ideiaRepository).delete(ideia);
        verify(auditService).registrarEvento(
                AuditEventType.IDEA_DELETED,
                1L,
                AuditResourceType.IDEA,
                "42",
                Map.of("previousStatus", "NOVA")
        );
    }

    @Test
    void nonOwnerCannotDeleteIdea() {
        Usuario owner = user(1L, "Owner");
        Usuario nonOwner = user(2L, "Non-owner");
        Ideia ideia = idea(42L, owner);

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(nonOwner);

        assertThrows(
                UnauthorizedActionException.class,
                () -> ideiaService.deletarIdeia(42L)
        );

        verify(ideiaRepository, never()).delete(any());
        verifyNoInteractions(auditService);
    }

    @Test
    void ownershipIsCheckedBeforeDuplicateTitleOrRelationshipLookup() {
        Usuario owner = user(1L, "Owner");
        Usuario nonOwner = user(2L, "Non-owner");
        Ideia ideia = idea(42L, owner);

        when(ideiaRepository.findById(42L)).thenReturn(Optional.of(ideia));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(nonOwner);
        stubUpdateDependencies(ideia);

        assertThrows(
                UnauthorizedActionException.class,
                () -> ideiaService.atualizarIdeia(42L, updateRequest())
        );

        verify(ideiaRepository, never()).existsByTitulo(any());
        verifyNoInteractions(categoriaService, orientacaoService);
        verify(ideiaRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    private RegistrarIdeiaDTO updateRequest() {
        return new RegistrarIdeiaDTO(
                20L,
                30L,
                "Novo titulo",
                "Nova descricao",
                "Novo problema",
                "Nova solucao"
        );
    }

    private void stubUpdateDependencies(Ideia ideia) {
        when(categoriaService.buscarEntidadePorId(20L))
                .thenReturn(category(20L, "Tecnologia"));
        when(orientacaoService.buscarEntidadePorId(30L))
                .thenReturn(orientation(30L, "Inovacao"));
        when(ideiaRepository.save(ideia)).thenReturn(ideia);
    }

    private Usuario user(Long id, String name) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(name);
        return usuario;
    }

    private Ideia idea(Long id, Usuario owner) {
        Ideia ideia = new Ideia();
        ideia.setId(id);
        ideia.setUsuario(owner);
        ideia.setCategoria(category(10L, "Operacoes"));
        ideia.setOrientacaoEstrategica(orientation(11L, "Eficiencia"));
        ideia.setStatusIdeia(new StatusIdeia(1L, NomeStatusIdeia.NOVA, "Nova"));
        ideia.setTitulo("Titulo existente");
        ideia.setDescricao("Descricao");
        ideia.setProblema("Problema");
        ideia.setSolucaoProposta("Solucao");
        ideia.setImpacto(0);
        ideia.setViabilidade(0);
        ideia.setPrioridade(0);
        ideia.setTotalVotos(0);
        return ideia;
    }

    private Categoria category(Long id, String name) {
        return new Categoria(id, name, "Descricao");
    }

    private OrientacaoEstrategica orientation(Long id, String title) {
        OrientacaoEstrategica orientacao = new OrientacaoEstrategica();
        orientacao.setId(id);
        orientacao.setTitulo(title);
        orientacao.setAreaEstrategica("Operacoes");
        return orientacao;
    }
}
