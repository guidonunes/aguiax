package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.config.security.SecurityConfig;
import br.com.fiap.aguiax.config.security.TokenService;
import br.com.fiap.aguiax.config.security.TokenVerification;
import br.com.fiap.aguiax.model.NomePerfil;
import br.com.fiap.aguiax.model.TipoPerfil;
import br.com.fiap.aguiax.model.Usuario;
import br.com.fiap.aguiax.repository.UsuarioRepository;
import br.com.fiap.aguiax.service.IdeiaService;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = IdeiaController.class,
        properties = "api.security.token.secret=jwt-test-secret-with-sufficient-length"
)
@Import({SecurityConfig.class, TokenVerification.class, TokenService.class})
class IdeiaStatusJwtIntegrationTest {

    private static final String JWT_TEST_SECRET =
            "jwt-test-secret-with-sufficient-length";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private IdeiaService ideiaService;

    private Logger securityLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void captureSecurityLogs() {
        securityLogger = (Logger) LoggerFactory.getLogger(SecurityConfig.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        securityLogger.addAppender(logAppender);
    }

    @AfterEach
    void stopCapturingSecurityLogs() {
        securityLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void validManagerJwtCanChangeIdeaStatus() throws Exception {
        Usuario gestor = user(17L, "gestor@example.com", NomePerfil.GESTOR);
        String token = tokenService.gerarToken(gestor);
        when(usuarioRepository.findByEmail(gestor.getEmail()))
                .thenReturn(Optional.of(gestor));
        when(ideiaService.atualizarStatusIdeia(any(), any())).thenReturn(null);

        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "EM_ANALISE"}
                                """))
                .andExpect(status().isOk());

        verify(ideiaService).atualizarStatusIdeia(any(), any());
    }

    @Test
    void invalidJwtReceives401BeforeCallingIdeaService() throws Exception {
        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "EM_ANALISE"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(ideiaService);

        String logs = capturedLogs();
        assertTrue(logs.contains(
                "Requisição não autenticada rejeitada: method=PATCH, "
                        + "path=/ideias/42/status"
        ));
        assertFalse(logs.contains("invalid-token"));
        assertFalse(logs.contains("Authorization"));
        assertFalse(logs.contains("Bearer"));
    }

    @Test
    void expiredJwtReceives401BeforeRepositoryOrServiceAccess() throws Exception {
        String expiredToken = JWT.create()
                .withIssuer("aguiax-api")
                .withSubject("gestor@example.com")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(JWT_TEST_SECRET));

        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + expiredToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "EM_ANALISE"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioRepository, ideiaService);
        assertFalse(capturedLogs().contains(expiredToken));
    }

    @Test
    void operationalUserReceives403AndAuthorizationLogContainsNoJwt() throws Exception {
        Usuario operacional = user(
                18L,
                "operacional@example.com",
                NomePerfil.OPERACIONAL
        );
        String token = tokenService.gerarToken(operacional);
        when(usuarioRepository.findByEmail(operacional.getEmail()))
                .thenReturn(Optional.of(operacional));

        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "EM_ANALISE"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(ideiaService);

        String logs = capturedLogs();
        assertTrue(logs.contains(
                "Acesso proibido: method=PATCH, path=/ideias/42/status"
        ));
        assertFalse(logs.contains(token));
        assertFalse(logs.contains("Authorization: Bearer"));
    }

    private String capturedLogs() {
        return logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .reduce("", (left, right) -> left + "\n" + right);
    }

    private Usuario user(Long id, String email, NomePerfil profile) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário de teste");
        usuario.setEmail(email);
        usuario.setStatus(1);
        usuario.setTipoPerfil(new TipoPerfil(2L, profile, profile.name()));
        return usuario;
    }
}
