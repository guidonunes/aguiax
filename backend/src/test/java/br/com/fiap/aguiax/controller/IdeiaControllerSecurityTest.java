package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiax.exception.InvalidRequestException;
import br.com.fiap.aguiax.service.IdeiaService;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        IdeiaController.class,
        ApplicationExceptionHandler.class,
        IdeiaControllerSecurityTest.TestSecurityConfiguration.class
})
class IdeiaControllerSecurityTest {

    private static final String VALID_IDEA_JSON = """
            {
              "categoriaId": 1,
              "orientacaoId": 1,
              "titulo": "Nova ideia",
              "descricao": "Descricao valida",
              "problema": "Problema",
              "solucaoProposta": "Solucao"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdeiaService ideiaService;

    @Test
    void unauthenticatedCallerReceives401() throws Exception {
        mockMvc.perform(post("/ideias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_IDEA_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(ideiaService);
    }

    @Test
    void operationalUserCanCreateIdea() throws Exception {
        when(ideiaService.criarIdeia(any())).thenReturn(null);

        mockMvc.perform(post("/ideias")
                        .with(user("operacional").roles("OPERACIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_IDEA_JSON))
                .andExpect(status().isCreated());

        verify(ideiaService).criarIdeia(any());
    }

    @Test
    void managerCannotCreateIdea() throws Exception {
        mockMvc.perform(post("/ideias")
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_IDEA_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(ideiaService);
    }

    @Test
    void leaderCannotCreateIdea() throws Exception {
        mockMvc.perform(post("/ideias")
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_IDEA_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(ideiaService);
    }

    @Test
    void managerCannotUpdateOrDeleteIdea() throws Exception {
        mockMvc.perform(put("/ideias/{id}", 42L)
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_IDEA_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/ideias/{id}", 42L)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(ideiaService);
    }

    @Test
    void leaderCannotUpdateOrDeleteIdea() throws Exception {
        mockMvc.perform(put("/ideias/{id}", 42L)
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_IDEA_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/ideias/{id}", 42L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(ideiaService);
    }

    @Test
    void onlyManagerCanChangeIdeaStatus() throws Exception {
        String statusJson = """
                {"status": "EM_ANALISE"}
                """;

        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .with(user("operacional").roles("OPERACIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isForbidden());

        when(ideiaService.atualizarStatusIdeia(any(), any())).thenReturn(null);

        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isOk());

        verify(ideiaService).atualizarStatusIdeia(any(), any());
    }

    @Test
    void invalidIdeaStatusTransitionReceivesControlled400() throws Exception {
        when(ideiaService.atualizarStatusIdeia(any(), any()))
                .thenThrow(new InvalidRequestException(
                        "Transição de status não permitida: NOVA -> APROVADA."
                ));

        mockMvc.perform(patch("/ideias/{id}/status", 42L)
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APROVADA"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Transição de status não permitida: NOVA -> APROVADA."
                ))
                .andExpect(jsonPath("$.path").value("/ideias/42/status"));

        verify(ideiaService).atualizarStatusIdeia(any(), any());
    }

    @Test
    void onlyManagerCanReviewIdea() throws Exception {
        String reviewJson = """
                {"impacto": 8, "viabilidade": 7}
                """;

        mockMvc.perform(patch("/ideias/{id}/avaliacao", 42L)
                        .with(user("operacional").roles("OPERACIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/ideias/{id}/avaliacao", 42L)
                        .with(user("lider").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isForbidden());

        when(ideiaService.atualizarAvaliacaoIdeia(any(), any())).thenReturn(null);

        mockMvc.perform(patch("/ideias/{id}/avaliacao", 42L)
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isOk());

        verify(ideiaService).atualizarAvaliacaoIdeia(any(), any());
    }

    @Test
    void invalidManagerReviewReceives400WithoutCallingService() throws Exception {
        mockMvc.perform(patch("/ideias/{id}/avaliacao", 42L)
                        .with(user("gestor").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"impacto": 11, "viabilidade": 7}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ideiaService);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(
                                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                            ))
                    .build();
        }
    }
}
