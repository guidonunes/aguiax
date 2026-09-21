package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.audit.model.AuditEventType;
import br.com.fiap.aguiax.audit.model.AuditResourceType;
import br.com.fiap.aguiax.audit.service.AuditService;
import br.com.fiap.aguiax.config.security.TokenService;
import br.com.fiap.aguiax.dto.perfil.LoginDTO;
import br.com.fiap.aguiax.dto.perfil.TokenDTO;
import br.com.fiap.aguiax.model.NomePerfil;
import br.com.fiap.aguiax.model.TipoPerfil;
import br.com.fiap.aguiax.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthControllerAuditTest {

    private AuthenticationManager authenticationManager;
    private TokenService tokenService;
    private AuditService auditService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        tokenService = mock(TokenService.class);
        auditService = mock(AuditService.class);
        authController = new AuthController(authenticationManager, tokenService, auditService);
    }

    @Test
    void shouldAuditOnlySuccessfulLoginWithoutCredentialsOrToken() {
        Usuario usuario = user(17L);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.gerarToken(usuario)).thenReturn("signed-token");

        ResponseEntity<TokenDTO> response = authController.login(
                new LoginDTO("user@example.com", "secret-password")
        );

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.hasBody());
        assertEquals("signed-token", response.getBody().token());
        verify(auditService).registrarEvento(
                AuditEventType.USER_LOGIN,
                17L,
                AuditResourceType.USER,
                "17",
                Map.of()
        );
    }

    @Test
    void shouldNotAuditRejectedLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authController.login(
                        new LoginDTO("user@example.com", "wrong-password")
                )
        );

        verifyNoInteractions(tokenService, auditService);
    }

    private Usuario user(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário");
        usuario.setEmail("user@example.com");
        usuario.setTipoPerfil(new TipoPerfil(1L, NomePerfil.OPERACIONAL, "Operacional"));
        return usuario;
    }
}
