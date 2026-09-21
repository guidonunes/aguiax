package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.service.BadgeService;
import br.com.fiap.aguiax.service.CategoriaService;
import br.com.fiap.aguiax.service.StatusIdeiaService;
import br.com.fiap.aguiax.service.StatusProjetoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        CategoriaController.class,
        BadgeController.class,
        StatusIdeiaController.class,
        StatusProjetoController.class,
        CatalogControllerSecurityTest.TestSecurityConfiguration.class
})
class CatalogControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private BadgeService badgeService;

    @MockitoBean
    private StatusIdeiaService statusIdeiaService;

    @MockitoBean
    private StatusProjetoService statusProjetoService;

    @Test
    void catalogReadsRequireAuthenticationAndAllowEveryApplicationRole() throws Exception {
        for (String path : new String[]{
                "/categorias",
                "/badges",
                "/status-ideias",
                "/status-projetos"
        }) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());

            for (String role : new String[]{"OPERACIONAL", "GESTOR", "LIDER"}) {
                mockMvc.perform(get(path).with(user(role.toLowerCase()).roles(role)))
                        .andExpect(status().isOk());
            }
        }

        verify(categoriaService, times(3)).listarCategorias();
        verify(badgeService, times(3)).listarBadges();
        verify(statusIdeiaService, times(3)).listarStatusIdeias();
        verify(statusProjetoService, times(3)).listarStatusProjetos();
    }

    @Test
    void migrationOwnedCatalogsExposeNoMutationRoute() throws Exception {
        for (String path : new String[]{
                "/badges",
                "/status-ideias",
                "/status-projetos"
        }) {
            mockMvc.perform(post(path).with(user("lider").roles("LIDER")))
                    .andExpect(status().isMethodNotAllowed());
        }

        verifyNoInteractions(badgeService, statusIdeiaService, statusProjetoService);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(
                                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                            ))
                    .build();
        }
    }
}
