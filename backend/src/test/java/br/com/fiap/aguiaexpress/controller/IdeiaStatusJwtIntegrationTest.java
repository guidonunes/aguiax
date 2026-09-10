package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.config.security.SecurityConfig;
import br.com.fiap.aguiaexpress.config.security.TokenService;
import br.com.fiap.aguiaexpress.config.security.TokenVerification;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import br.com.fiap.aguiaexpress.service.IdeiaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private IdeiaService ideiaService;

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
