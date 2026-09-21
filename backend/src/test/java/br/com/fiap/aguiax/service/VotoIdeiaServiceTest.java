package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.dto.ideia.MostrarVotoIdeiaDTO;
import br.com.fiap.aguiax.exception.DuplicateResourceException;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.Ideia;
import br.com.fiap.aguiax.model.StatusIdeia;
import br.com.fiap.aguiax.model.Usuario;
import br.com.fiap.aguiax.model.VotoIdeia;
import br.com.fiap.aguiax.repository.IdeiaRepository;
import br.com.fiap.aguiax.repository.UsuarioRepository;
import br.com.fiap.aguiax.repository.VotoIdeiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VotoIdeiaServiceTest {

    private VotoIdeiaRepository votoRepository;
    private IdeiaRepository ideiaRepository;
    private IdeiaService ideiaService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private IdeaPriorityService ideaPriorityService;
    private VotoIdeiaService service;

    @BeforeEach
    void setUp() {
        votoRepository = mock(VotoIdeiaRepository.class);
        ideiaRepository = mock(IdeiaRepository.class);
        ideiaService = mock(IdeiaService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        ideaPriorityService = mock(IdeaPriorityService.class);

        service = new VotoIdeiaService(
                votoRepository,
                mock(UsuarioRepository.class),
                ideiaService,
                ideiaRepository,
                usuarioAutenticadoService,
                ideaPriorityService
        );
    }

    @Test
    void shouldRecalculatePriorityAfterVoteCreation() {
        Usuario actor = usuario();
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(actor);
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(votoRepository.existsByUsuarioAndIdeia(actor, ideia))
                .thenReturn(false);
        when(votoRepository.save(any(VotoIdeia.class)))
                .thenAnswer(invocation -> {
                    VotoIdeia voto = invocation.getArgument(0);
                    voto.setId(91L);
                    return voto;
                });
        when(votoRepository.countByIdeia(ideia)).thenReturn(6L);
        when(ideaPriorityService.recalculateWithLatestEvaluation(ideia))
                .thenAnswer(invocation -> {
                    ideia.setPrioridade(new BigDecimal("7.55"));
                    return ideia.getPrioridade();
                });
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        MostrarVotoIdeiaDTO response = service.votarIdeia(42L);

        assertEquals(91L, response.id());
        assertEquals(6, ideia.getTotalVotos());
        assertEquals(new BigDecimal("7.55"), ideia.getPrioridade());
        assertSame(originalStatus, ideia.getStatusIdeia());

        InOrder order = inOrder(
                votoRepository,
                ideaPriorityService,
                ideiaRepository
        );
        order.verify(votoRepository).save(any(VotoIdeia.class));
        order.verify(votoRepository).countByIdeia(ideia);
        order.verify(
                ideaPriorityService
        ).recalculateWithLatestEvaluation(ideia);
        order.verify(ideiaRepository).save(ideia);
    }

    @Test
    void shouldRejectDuplicateVoteWithoutMutatingIdea() {
        Usuario actor = usuario();
        Ideia ideia = ideia();
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(actor);
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(votoRepository.existsByUsuarioAndIdeia(actor, ideia))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> service.votarIdeia(42L)
        );

        assertEquals("Usuário já votou nessa ideia.", exception.getMessage());
        verify(votoRepository).existsByUsuarioAndIdeia(actor, ideia);
        verify(votoRepository, never()).save(any());
        verifyNoInteractions(ideaPriorityService, ideiaRepository);
    }

    @Test
    void shouldRecalculatePriorityAfterVoteRemoval() {
        Usuario actor = usuario();
        Ideia ideia = ideia();
        StatusIdeia originalStatus = ideia.getStatusIdeia();
        VotoIdeia voto = new VotoIdeia();
        voto.setId(91L);
        voto.setUsuario(actor);
        voto.setIdeia(ideia);

        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(actor);
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(votoRepository.findByUsuarioAndIdeia(actor, ideia))
                .thenReturn(Optional.of(voto));
        when(votoRepository.countByIdeia(ideia)).thenReturn(4L);
        when(ideaPriorityService.recalculateWithLatestEvaluation(ideia))
                .thenAnswer(invocation -> {
                    ideia.setPrioridade(new BigDecimal("7.15"));
                    return ideia.getPrioridade();
                });
        when(ideiaRepository.save(ideia)).thenReturn(ideia);

        service.removerVoto(42L);

        assertEquals(4, ideia.getTotalVotos());
        assertEquals(new BigDecimal("7.15"), ideia.getPrioridade());
        assertSame(originalStatus, ideia.getStatusIdeia());

        InOrder order = inOrder(
                votoRepository,
                ideaPriorityService,
                ideiaRepository
        );
        order.verify(votoRepository).delete(voto);
        order.verify(votoRepository).countByIdeia(ideia);
        order.verify(
                ideaPriorityService
        ).recalculateWithLatestEvaluation(ideia);
        order.verify(ideiaRepository).save(ideia);
    }

    @Test
    void shouldRejectRemovalWhenAuthenticatedUserHasNoVote() {
        Usuario actor = usuario();
        Ideia ideia = ideia();
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(actor);
        when(ideiaService.buscarEntidadePorId(42L)).thenReturn(ideia);
        when(votoRepository.findByUsuarioAndIdeia(actor, ideia))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.removerVoto(42L)
        );

        assertEquals(
                "Voto não encontrado para esse usuário e ideia.",
                exception.getMessage()
        );
        verify(votoRepository).findByUsuarioAndIdeia(actor, ideia);
        verify(votoRepository, never()).delete(any());
        verifyNoInteractions(ideaPriorityService, ideiaRepository);
    }

    @Test
    void voteMutationsShouldBeTransactional() throws Exception {
        Method create = VotoIdeiaService.class.getMethod(
                "votarIdeia",
                Long.class
        );
        Method remove = VotoIdeiaService.class.getMethod(
                "removerVoto",
                Long.class
        );

        assertTrue(create.isAnnotationPresent(Transactional.class));
        assertTrue(remove.isAnnotationPresent(Transactional.class));
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(17L);
        usuario.setNome("Gestor");
        usuario.setEmail("gestor@example.com");
        return usuario;
    }

    private Ideia ideia() {
        Ideia ideia = new Ideia();
        ideia.setId(42L);
        ideia.setTitulo("Roteirização inteligente");
        ideia.setImpacto(8);
        ideia.setViabilidade(6);
        ideia.setTotalVotos(5);
        ideia.setPrioridade(new BigDecimal("7.35"));
        ideia.setStatusIdeia(new StatusIdeia());
        return ideia;
    }
}
