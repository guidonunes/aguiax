package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.AtualizarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.MostrarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarUsuarioDTO;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.TipoPerfilRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UsuarioServiceAuthorizationTest {

    private UsuarioAutenticadoService usuarioAutenticadoService;
    private UsuarioRepository usuarioRepository;
    private TipoPerfilRepository tipoPerfilRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        tipoPerfilRepository = mock(TipoPerfilRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        usuarioService = new UsuarioService(
                usuarioAutenticadoService,
                usuarioRepository,
                tipoPerfilRepository,
                passwordEncoder
        );
    }

    @Test
    void publicRegistrationRejectsPrivilegedProfilesBeforePersistence() {
        for (NomePerfil perfil : new NomePerfil[]{NomePerfil.GESTOR, NomePerfil.LIDER}) {
            RegistrarUsuarioDTO request = registration(perfil, perfil.name().toLowerCase() + "@example.com");

            assertThrows(
                    InvalidRequestException.class,
                    () -> usuarioService.salvarUsuario(request)
            );
        }

        verifyNoInteractions(usuarioRepository, tipoPerfilRepository, passwordEncoder);
    }

    @Test
    void publicRegistrationCreatesOnlyOperationalUser() {
        TipoPerfil operacional = profile(NomePerfil.OPERACIONAL);
        RegistrarUsuarioDTO request = registration(NomePerfil.OPERACIONAL, "op@example.com");

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tipoPerfilRepository.findByNomePerfil(NomePerfil.OPERACIONAL))
                .thenReturn(Optional.of(operacional));
        when(passwordEncoder.encode(request.senha())).thenReturn("bcrypt-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        MostrarUsuarioDTO result = usuarioService.salvarUsuario(request);

        assertEquals(NomePerfil.OPERACIONAL, result.perfil());
        assertEquals("op@example.com", result.email());
        verify(tipoPerfilRepository).findByNomePerfil(NomePerfil.OPERACIONAL);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void adminProvisioningUsesRequestedExistingProfile() {
        TipoPerfil lider = profile(NomePerfil.LIDER);
        RegistrarUsuarioDTO request = registration(NomePerfil.LIDER, "lider@example.com");

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(tipoPerfilRepository.findByNomePerfil(NomePerfil.LIDER))
                .thenReturn(Optional.of(lider));
        when(passwordEncoder.encode(request.senha())).thenReturn("bcrypt-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MostrarUsuarioDTO result = usuarioService.salvarUsuarioAdmin(request);

        assertEquals(NomePerfil.LIDER, result.perfil());
        verify(tipoPerfilRepository).findByNomePerfil(NomePerfil.LIDER);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void userCanReadOwnProfileByMatchingPersistedId() {
        Usuario target = user(7L, NomePerfil.OPERACIONAL, "target@example.com");
        Usuario authenticated = user(7L, NomePerfil.OPERACIONAL, "auth@example.com");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(authenticated);

        MostrarUsuarioDTO result = usuarioService.buscarUsuarioPorId(7L);

        assertEquals(7L, result.id());
    }

    @Test
    void leaderCanReadAnotherUsersProfile() {
        Usuario target = user(7L, NomePerfil.OPERACIONAL, "target@example.com");
        Usuario leader = user(99L, NomePerfil.LIDER, "leader@example.com");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(leader);

        MostrarUsuarioDTO result = usuarioService.buscarUsuarioPorId(7L);

        assertEquals(7L, result.id());
    }

    @Test
    void nonOwnerCannotReadAnotherUsersProfile() {
        Usuario target = user(7L, NomePerfil.OPERACIONAL, "target@example.com");
        Usuario caller = user(8L, NomePerfil.OPERACIONAL, "caller@example.com");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(caller);

        assertThrows(
                UnauthorizedActionException.class,
                () -> usuarioService.buscarUsuarioPorId(7L)
        );
    }

    @Test
    void nonOwnerCannotUpdateAndAuthorizationRunsBeforeDuplicateCheck() {
        Usuario target = user(7L, NomePerfil.OPERACIONAL, "target@example.com");
        Usuario caller = user(8L, NomePerfil.OPERACIONAL, "caller@example.com");
        AtualizarUsuarioDTO request = update("other@example.com");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(caller);

        assertThrows(
                UnauthorizedActionException.class,
                () -> usuarioService.atualizarUsuario(7L, request)
        );

        verify(usuarioRepository, never()).existsByEmail(any());
        verify(usuarioRepository, never()).save(any());
        assertEquals("target@example.com", target.getEmail());
    }

    @Test
    void leaderCanUpdateAnotherUserWithoutChangingProtectedFields() {
        Usuario target = user(7L, NomePerfil.GESTOR, "old@example.com");
        target.setNome("Old name");
        target.setSenhaHash("existing-hash");
        target.setPontos(123);
        target.setStatus(1);
        target.setDataCriacao(LocalDateTime.of(2026, 1, 1, 10, 0));
        TipoPerfil originalProfile = target.getTipoPerfil();
        Usuario leader = user(99L, NomePerfil.LIDER, "leader@example.com");
        AtualizarUsuarioDTO request = update("new@example.com");

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(target));
        when(usuarioAutenticadoService.getUsuarioAutenticado()).thenReturn(leader);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.save(target)).thenReturn(target);

        MostrarUsuarioDTO result = usuarioService.atualizarUsuario(7L, request);

        assertEquals("New name", result.nome());
        assertEquals("new@example.com", result.email());
        assertSame(originalProfile, target.getTipoPerfil());
        assertEquals("existing-hash", target.getSenhaHash());
        assertEquals(123, target.getPontos());
        assertEquals(1, target.getStatus());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), target.getDataCriacao());
        verify(usuarioRepository).save(target);
    }

    private RegistrarUsuarioDTO registration(NomePerfil perfil, String email) {
        return new RegistrarUsuarioDTO(
                "Test user",
                email,
                "secret123",
                perfil,
                "Analista",
                "Operacoes"
        );
    }

    private AtualizarUsuarioDTO update(String email) {
        return new AtualizarUsuarioDTO(
                "New name",
                email,
                "New role title",
                "New department"
        );
    }

    private TipoPerfil profile(NomePerfil nomePerfil) {
        return new TipoPerfil(1L, nomePerfil, nomePerfil.name());
    }

    private Usuario user(Long id, NomePerfil perfil, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setTipoPerfil(profile(perfil));
        usuario.setNome("User " + id);
        usuario.setEmail(email);
        return usuario;
    }
}
