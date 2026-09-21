package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.exception.UnauthorizedActionException;
import br.com.fiap.aguiax.model.Badge;
import br.com.fiap.aguiax.model.NomePerfil;
import br.com.fiap.aguiax.model.TipoPerfil;
import br.com.fiap.aguiax.model.Usuario;
import br.com.fiap.aguiax.model.UsuarioBadge;
import br.com.fiap.aguiax.repository.UsuarioBadgeRepository;
import br.com.fiap.aguiax.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioBadgeServiceAuthorizationTest {

    private UsuarioAutenticadoService usuarioAutenticadoService;
    private UsuarioBadgeRepository usuarioBadgeRepository;
    private UsuarioRepository usuarioRepository;
    private UsuarioBadgeService usuarioBadgeService;

    @BeforeEach
    void setUp() {
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        usuarioBadgeRepository = mock(UsuarioBadgeRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        BadgeService badgeService = mock(BadgeService.class);
        usuarioBadgeService = new UsuarioBadgeService(
                usuarioAutenticadoService,
                usuarioBadgeRepository,
                usuarioRepository,
                badgeService
        );
    }

    @Test
    void operationalUserCanListOwnBadgesByMatchingPersistedId() {
        Usuario target = user(7L, NomePerfil.OPERACIONAL);
        Usuario authenticated = user(7L, NomePerfil.OPERACIONAL);

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(authenticated);
        when(usuarioBadgeRepository.findByUsuario(target)).thenReturn(List.of());

        assertEquals(0, usuarioBadgeService.listarBadgesPorUsuario(7L).size());
        verify(usuarioBadgeRepository).findByUsuario(target);
    }

    @Test
    void operationalUserCannotListAnotherUsersBadges() {
        Usuario target = user(7L, NomePerfil.OPERACIONAL);
        Usuario caller = user(8L, NomePerfil.OPERACIONAL);

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(caller);

        assertThrows(
                UnauthorizedActionException.class,
                () -> usuarioBadgeService.listarBadgesPorUsuario(7L)
        );

        verify(usuarioBadgeRepository, never()).findByUsuario(target);
    }

    @Test
    void managerAndLeaderCanListAnotherUsersBadges() {
        for (NomePerfil perfil : new NomePerfil[]{NomePerfil.GESTOR, NomePerfil.LIDER}) {
            Usuario target = user(7L, NomePerfil.OPERACIONAL);
            Usuario caller = user(8L, perfil);

            when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
            when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(caller);
            when(usuarioBadgeRepository.findByUsuario(target)).thenReturn(List.of());

            assertEquals(0, usuarioBadgeService.listarBadgesPorUsuario(7L).size());
            verify(usuarioBadgeRepository).findByUsuario(target);
        }
    }

    @Test
    void badgeDetailOwnershipUsesRecipientIdNotAwardId() {
        Usuario recipient = user(7L, NomePerfil.OPERACIONAL);
        Usuario callerWhoseIdMatchesAward = user(42L, NomePerfil.OPERACIONAL);
        Badge badge = new Badge(3L, "Badge", "Description", "Criteria", "icon", 1);
        UsuarioBadge award = new UsuarioBadge(
                42L,
                recipient,
                badge,
                LocalDateTime.now()
        );

        when(usuarioBadgeRepository.findById(42L)).thenReturn(Optional.of(award));
        when(usuarioAutenticadoService.getUsuarioAutenticado())
                .thenReturn(callerWhoseIdMatchesAward);

        assertThrows(
                UnauthorizedActionException.class,
                () -> usuarioBadgeService.buscarUsuarioBadgePorId(42L)
        );
    }

    private Usuario user(Long id, NomePerfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setTipoPerfil(new TipoPerfil(1L, perfil, perfil.name()));
        return usuario;
    }
}
