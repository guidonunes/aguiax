package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.service.CategoriaService;
import br.com.fiap.aguiaexpress.service.UsuarioBadgeService;
import br.com.fiap.aguiaexpress.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        UsuarioController.class,
        CategoriaController.class,
        UsuarioBadgeController.class,
        ApplicationExceptionHandler.class,
        AdministrativeControllerSecurityTest.TestSecurityConfiguration.class
})
class AdministrativeControllerSecurityTest {

    private static final String VALID_USER_JSON = """
            {
              "nome": "New user",
              "email": "new.user@example.com",
              "senha": "secret123",
              "nomePerfil": "OPERACIONAL",
              "cargo": "Analista",
              "departamento": "Operacoes"
            }
            """;

    private static final String VALID_CATEGORY_JSON = """
            {
              "nomeCategoria": "Tecnologia",
              "descricao": "Ideias de tecnologia"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private UsuarioBadgeService usuarioBadgeService;

    @Test
    void publicRegistrationRemainsPublic() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isCreated());

        verify(usuarioService).salvarUsuario(any());
    }

    @Test
    void publicPrivilegedRegistrationReturnsBadRequest() throws Exception {
        String privilegedUserJson = VALID_USER_JSON.replace(
                "\"OPERACIONAL\"",
                "\"LIDER\""
        );
        doThrow(new InvalidRequestException(
                "O cadastro público permite apenas o perfil OPERACIONAL."
        )).when(usuarioService).salvarUsuario(any());

        mockMvc.perform(post("/usuarios")
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privilegedUserJson))
                .andExpect(status().isBadRequest());

        verify(usuarioService).salvarUsuario(any());
    }

    @Test
    void adminProvisioningRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/usuarios/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void onlyLeaderCanProvisionUsers() throws Exception {
        mockMvc.perform(post("/usuarios/admin")
                        .with(user("operacional").roles("OPERACIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios/admin")
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios/admin")
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isCreated());

        verify(usuarioService).salvarUsuarioAdmin(any());
    }

    @Test
    void onlyLeaderCanListAndDeleteUsers() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .with(user("operacional").roles("OPERACIONAL")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/usuarios/{id}", 7L)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/usuarios")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/usuarios/{id}", 7L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isNoContent());

        verify(usuarioService).listarTodosUsuarios();
        verify(usuarioService).deletarUsuarioPorId(7L);
    }

    @Test
    void categoryMutationsAreLeaderOnly() throws Exception {
        mockMvc.perform(post("/categorias")
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CATEGORY_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/categorias/{id}", 3L)
                        .with(user("operacional").roles("OPERACIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CATEGORY_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/categorias/{id}", 3L)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);

        mockMvc.perform(post("/categorias")
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CATEGORY_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/categorias/{id}", 3L)
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CATEGORY_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/categorias/{id}", 3L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isNoContent());

        verify(categoriaService).criarCategoria(any());
        verify(categoriaService).atualizarCategoria(any(), any());
        verify(categoriaService).deletarCategoria(3L);
    }

    @Test
    void badgeAwardMutationsAreManagerOnly() throws Exception {
        mockMvc.perform(post("/usuario-badges/usuario/{usuarioId}/badge/{badgeId}", 7L, 3L)
                        .with(user("operacional").roles("OPERACIONAL")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuario-badges/usuario/{usuarioId}/nome/{nomeBadge}", 7L, "INOVADOR")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/usuario-badges/usuario/{usuarioId}/badge/{badgeId}", 7L, 3L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioBadgeService);

        mockMvc.perform(post("/usuario-badges/usuario/{usuarioId}/badge/{badgeId}", 7L, 3L)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/usuario-badges/usuario/{usuarioId}/nome/{nomeBadge}", 7L, "INOVADOR")
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/usuario-badges/usuario/{usuarioId}/badge/{badgeId}", 7L, 3L)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isNoContent());

        verify(usuarioBadgeService).concederBadge(7L, 3L);
        verify(usuarioBadgeService).concederBadgePorNome(7L, "INOVADOR");
        verify(usuarioBadgeService).removerBadgeDoUsuario(7L, 3L);
    }

    @Test
    void globalAwardReadsRequireManagerOrLeader() throws Exception {
        mockMvc.perform(get("/usuario-badges")
                        .with(user("operacional").roles("OPERACIONAL")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/usuario-badges/badge/{badgeId}", 3L)
                        .with(user("operacional").roles("OPERACIONAL")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioBadgeService);

        mockMvc.perform(get("/usuario-badges")
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuario-badges/badge/{badgeId}", 3L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk());

        verify(usuarioBadgeService).listarUsuarioBadges();
        verify(usuarioBadgeService).listarUsuariosPorBadge(3L);
    }

    @Test
    void individualAwardReadDelegatesOwnershipToService() throws Exception {
        mockMvc.perform(get("/usuario-badges/usuario/{usuarioId}", 7L)
                        .with(user("operacional").roles("OPERACIONAL")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuario-badges/{id}", 42L)
                        .with(user("operacional").roles("OPERACIONAL")))
                .andExpect(status().isOk());

        verify(usuarioBadgeService).listarBadgesPorUsuario(7L);
        verify(usuarioBadgeService).buscarUsuarioBadgePorId(42L);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                            .anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(
                                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                            ))
                    .build();
        }
    }
}
