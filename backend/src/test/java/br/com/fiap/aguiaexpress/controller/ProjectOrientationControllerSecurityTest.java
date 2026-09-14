package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiaexpress.service.OrientacaoEstrategicaService;
import br.com.fiap.aguiaexpress.service.ProjetoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        ProjetoController.class,
        OrientacaoEstrategicaController.class,
        ApplicationExceptionHandler.class,
        ProjectOrientationControllerSecurityTest.TestSecurityConfiguration.class
})
class ProjectOrientationControllerSecurityTest {

    private static final String VALID_PROJECT_JSON = """
            {
              "ideiaId": 11,
              "usuarioId": 7,
              "titulo": "Projeto de teste"
            }
            """;

    private static final String VALID_ORIENTATION_JSON = """
            {
              "titulo": "Eficiência operacional",
              "descricao": "Reduzir desperdícios",
              "areaEstrategica": "Operações"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjetoService projetoService;

    @MockitoBean
    private OrientacaoEstrategicaService orientacaoService;

    @Test
    void projectAndOrientationMutationsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PROJECT_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/orientacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORIENTATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projetoService, orientacaoService);
    }

    @Test
    void onlyManagerCanCreateProjects() throws Exception {
        for (String role : new String[]{"OPERACIONAL", "LIDER"}) {
            mockMvc.perform(post("/projetos")
                            .with(user(role.toLowerCase()).roles(role))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_PROJECT_JSON))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(projetoService);

        mockMvc.perform(post("/projetos")
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PROJECT_JSON))
                .andExpect(status().isCreated());

        verify(projetoService).criarProjeto(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "investimento",
            "roi",
            "retornoFinanceiro",
            "reducaoCustos",
            "ganhoProdutividade"
    })
    void managerCannotCreateProjectWithNegativeFinancialValue(
            String financialField
    ) throws Exception {
        String invalidRequest = """
                {
                  "ideiaId": 11,
                  "usuarioId": 7,
                  "titulo": "Projeto de teste",
                  "%s": -0.01
                }
                """.formatted(financialField);

        mockMvc.perform(post("/projetos")
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projetoService);
    }

    @Test
    void onlyLeaderCanCreateStrategicOrientations() throws Exception {
        for (String role : new String[]{"OPERACIONAL", "GESTOR"}) {
            mockMvc.perform(post("/orientacoes")
                            .with(user(role.toLowerCase()).roles(role))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_ORIENTATION_JSON))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(orientacaoService);

        mockMvc.perform(post("/orientacoes")
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORIENTATION_JSON))
                .andExpect(status().isCreated());

        verify(orientacaoService).criarOrientacao(any());
    }

    @Test
    void everyAuthenticatedRoleCanViewProjectsAndOrientations() throws Exception {
        when(projetoService.listarProjetos()).thenReturn(List.of());
        when(orientacaoService.listarOrientacoes()).thenReturn(List.of());

        for (String role : new String[]{"OPERACIONAL", "GESTOR", "LIDER"}) {
            mockMvc.perform(get("/projetos")
                            .with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/orientacoes")
                            .with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isOk());
        }

        verify(projetoService, times(3)).listarProjetos();
        verify(orientacaoService, times(3)).listarOrientacoes();
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
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
