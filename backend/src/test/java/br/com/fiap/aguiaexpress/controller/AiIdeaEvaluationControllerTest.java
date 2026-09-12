package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.ai.client.AiProviderFailure;
import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResponse;
import br.com.fiap.aguiaexpress.exception.AiEvaluationPersistenceException;
import br.com.fiap.aguiaexpress.exception.AiEvaluationUnavailableException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.service.AiIdeaEvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        AiIdeaEvaluationController.class,
        ApplicationExceptionHandler.class,
        AiIdeaEvaluationControllerTest.TestSecurityConfiguration.class
})
class AiIdeaEvaluationControllerTest {

    private static final String ENDPOINT = "/api/ideas/42/ai-evaluation";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiIdeaEvaluationService service;

    @Test
    void unauthenticatedCallerReceives401() throws Exception {
        mockMvc.perform(post(ENDPOINT))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void operationalAndLeaderUsersReceive403() throws Exception {
        for (String role : new String[]{"OPERACIONAL", "LIDER"}) {
            mockMvc.perform(post(ENDPOINT)
                            .with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(service);
    }

    @Test
    void managerReceivesStructuredEvaluation() throws Exception {
        when(service.avaliarIdeia(42L)).thenReturn(response());

        mockMvc.perform(post(ENDPOINT)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ideaId").value(42))
                .andExpect(jsonPath("$.strategicAlignment").value(9))
                .andExpect(jsonPath("$.impact").value(8))
                .andExpect(jsonPath("$.feasibility").value(7))
                .andExpect(jsonPath("$.innovation").value(8))
                .andExpect(jsonPath("$.overallScore").value(8.1))
                .andExpect(jsonPath("$.summary").value("Resumo validado."));

        verify(service).avaliarIdeia(42L);
    }

    @Test
    void unknownIdeaReceivesControlled404() throws Exception {
        when(service.avaliarIdeia(42L)).thenThrow(
                new ResourceNotFoundException("Ideia não encontrada.")
        );

        mockMvc.perform(post(ENDPOINT)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ideia não encontrada."))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    @Test
    void disabledAiReceivesControlled503() throws Exception {
        when(service.avaliarIdeia(42L)).thenThrow(
                new AiEvaluationUnavailableException(
                        "A avaliação por IA não está habilitada."
                )
        );

        mockMvc.perform(post(ENDPOINT)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value(
                        "A avaliação por IA não está habilitada."
                ));
    }

    @Test
    void unavailableEvaluationHistoryReceivesControlled503() throws Exception {
        when(service.avaliarIdeia(42L)).thenThrow(
                new AiEvaluationPersistenceException(
                        "Não foi possível armazenar o histórico da avaliação de IA.",
                        new DataAccessResourceFailureException(
                                "mongo unavailable"
                        )
                )
        );

        mockMvc.perform(post(ENDPOINT)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value(
                        "Não foi possível armazenar o histórico da avaliação de IA."
                ))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    @ParameterizedTest
    @MethodSource("providerFailureStatuses")
    void providerFailuresReceiveControlledHttpStatus(
            AiProviderFailure failure,
            int expectedStatus
    ) throws Exception {
        when(service.avaliarIdeia(42L)).thenThrow(new AiProviderException(
                failure,
                "Falha controlada do provedor de IA."
        ));

        mockMvc.perform(post(ENDPOINT)
                        .with(user("gestor").roles("GESTOR")))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.message").value(
                        "Falha controlada do provedor de IA."
                ))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    private static Stream<Arguments> providerFailureStatuses() {
        return Stream.of(
                Arguments.of(
                        AiProviderFailure.REQUEST_BUILD_FAILED,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ),
                Arguments.of(
                        AiProviderFailure.TIMEOUT,
                        HttpStatus.GATEWAY_TIMEOUT.value()
                ),
                Arguments.of(
                        AiProviderFailure.RATE_LIMITED,
                        HttpStatus.SERVICE_UNAVAILABLE.value()
                ),
                Arguments.of(
                        AiProviderFailure.UNAVAILABLE,
                        HttpStatus.SERVICE_UNAVAILABLE.value()
                ),
                Arguments.of(
                        AiProviderFailure.REJECTED_REQUEST,
                        HttpStatus.BAD_GATEWAY.value()
                ),
                Arguments.of(
                        AiProviderFailure.INVALID_RESPONSE,
                        HttpStatus.BAD_GATEWAY.value()
                )
        );
    }

    private AiIdeaEvaluationResponse response() {
        return new AiIdeaEvaluationResponse(
                42L,
                9,
                8,
                7,
                8,
                new BigDecimal("8.1"),
                "Resumo validado."
        );
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
