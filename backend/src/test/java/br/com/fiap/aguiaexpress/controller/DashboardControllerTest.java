package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.ai.client.AiProviderFailure;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardInsightResponse;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardInsightScope;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.exception.DashboardInsightPersistenceException;
import br.com.fiap.aguiaexpress.exception.DashboardInsightUnavailableException;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.service.DashboardInsightService;
import br.com.fiap.aguiaexpress.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        DashboardController.class,
        ApplicationExceptionHandler.class,
        DashboardControllerTest.TestSecurityConfiguration.class
})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private DashboardInsightService dashboardInsightService;

    @Test
    void unauthenticatedCallerReceives401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(dashboardService);
    }

    @Test
    void operationalAndManagerUsersReceive403ForGlobalSummary() throws Exception {
        for (String role : new String[]{"OPERACIONAL", "GESTOR"}) {
            mockMvc.perform(get("/api/dashboard/summary")
                            .with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(dashboardService);
    }

    @Test
    void operationalAndManagerUsersReceive403ForFilteredSummary() throws Exception {
        for (String role : new String[]{"OPERACIONAL", "GESTOR"}) {
            mockMvc.perform(get("/api/dashboard/summary")
                            .queryParam("orientationId", "42")
                            .with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(dashboardService);
    }

    @Test
    void leaderReceivesTheGlobalDashboardSummary() throws Exception {
        when(dashboardService.buscarResumo(null)).thenReturn(response());

        mockMvc.perform(get("/api/dashboard/summary")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2))
                .andExpect(jsonPath("$.projectsByStatus.PLANEJADO").value(1))
                .andExpect(jsonPath("$.projectsByStatus.EM_ANDAMENTO").value(1))
                .andExpect(jsonPath("$.projectsByStatus.CONCLUIDO").value(0))
                .andExpect(jsonPath("$.projectsByStatus.CANCELADO").value(0))
                .andExpect(jsonPath("$.totalInvestment").value(300.00))
                .andExpect(jsonPath("$.totalFinancialReturn").value(450.00))
                .andExpect(jsonPath("$.totalCostReduction").value(50.00))
                .andExpect(jsonPath("$.estimatedResult").value(200.00))
                .andExpect(jsonPath("$.averageRoi").value(15.00))
                .andExpect(jsonPath("$.averageProductivityGain").value(10.00));

        verify(dashboardService).buscarResumo(null);
    }

    @Test
    void leaderCanFilterTheSummaryByStrategicOrientation() throws Exception {
        when(dashboardService.buscarResumo(42L)).thenReturn(response());

        mockMvc.perform(get("/api/dashboard/summary")
                        .queryParam("orientationId", "42")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2));

        verify(dashboardService).buscarResumo(42L);
    }

    @Test
    void unknownOrientationReceivesControlled404() throws Exception {
        when(dashboardService.buscarResumo(404L))
                .thenThrow(new ResourceNotFoundException(
                        "Orientação estratégica não encontrada."
                ));

        mockMvc.perform(get("/api/dashboard/summary")
                        .queryParam("orientationId", "404")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "Orientação estratégica não encontrada."
                ))
                .andExpect(jsonPath("$.path").value("/api/dashboard/summary"));

        verify(dashboardService).buscarResumo(404L);
    }

    @Test
    void unauthenticatedCallerReceives401ForDashboardInsight() throws Exception {
        mockMvc.perform(post("/api/dashboard/insights"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(dashboardInsightService);
    }

    @Test
    void operationalAndManagerUsersReceive403ForDashboardInsight() throws Exception {
        for (String role : new String[]{"OPERACIONAL", "GESTOR"}) {
            mockMvc.perform(post("/api/dashboard/insights")
                            .with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(dashboardInsightService);
    }

    @Test
    void leaderReceivesPersistedStructuredDashboardInsight() throws Exception {
        when(dashboardInsightService.gerarInsight(null))
                .thenReturn(insightResponse(null));

        mockMvc.perform(post("/api/dashboard/insights")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("GLOBAL"))
                .andExpect(jsonPath("$.orientationId").doesNotExist())
                .andExpect(jsonPath("$.metricsSnapshot.totalProjects").value(2))
                .andExpect(jsonPath("$.executiveSummary").value(
                        "O portfólio apresenta resultado estimado positivo."
                ))
                .andExpect(jsonPath("$.highlights[0]").value(
                        "O retorno supera o investimento."
                ))
                .andExpect(jsonPath("$.attentionPoints[0]").value(
                        "Um projeto ainda está em andamento."
                ))
                .andExpect(jsonPath("$.recommendations[0]").value(
                        "Acompanhar a realização do retorno."
                ))
                .andExpect(jsonPath("$.generatedAt").value(
                        "2026-09-16T15:00:00Z"
                ));

        verify(dashboardInsightService).gerarInsight(null);
    }

    @Test
    void leaderCanRequestInsightForOneStrategicOrientation() throws Exception {
        when(dashboardInsightService.gerarInsight(42L))
                .thenReturn(insightResponse(42L));

        mockMvc.perform(post("/api/dashboard/insights")
                        .queryParam("orientationId", "42")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ORIENTATION"))
                .andExpect(jsonPath("$.orientationId").value(42));

        verify(dashboardInsightService).gerarInsight(42L);
    }

    @Test
    void emptySnapshotReceivesControlled400() throws Exception {
        when(dashboardInsightService.gerarInsight(null)).thenThrow(
                new InvalidRequestException(
                        "Não há projetos no dashboard para gerar uma análise executiva."
                )
        );

        mockMvc.perform(post("/api/dashboard/insights")
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Não há projetos no dashboard para gerar uma análise executiva."
                ));
    }

    @Test
    void dashboardInsightFailuresUseTheDocumentedHttpStatuses() throws Exception {
        when(dashboardInsightService.gerarInsight(1L)).thenThrow(
                new AiProviderException(
                        AiProviderFailure.INVALID_RESPONSE,
                        "O provedor de IA retornou uma análise inválida."
                )
        );
        when(dashboardInsightService.gerarInsight(2L)).thenThrow(
                new DashboardInsightUnavailableException(
                        "A análise executiva por IA não está habilitada."
                )
        );
        when(dashboardInsightService.gerarInsight(3L)).thenThrow(
                new AiProviderException(
                        AiProviderFailure.TIMEOUT,
                        "O provedor de IA excedeu o tempo limite."
                )
        );
        when(dashboardInsightService.gerarInsight(4L)).thenThrow(
                new DashboardInsightPersistenceException(
                        "Não foi possível armazenar o histórico da análise executiva.",
                        new RuntimeException("mongo unavailable")
                )
        );

        assertInsightStatus(1L, 502);
        assertInsightStatus(2L, 503);
        assertInsightStatus(3L, 504);
        assertInsightStatus(4L, 503);
    }

    private void assertInsightStatus(Long orientationId, int statusCode)
            throws Exception {
        mockMvc.perform(post("/api/dashboard/insights")
                        .queryParam("orientationId", orientationId.toString())
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.path").value("/api/dashboard/insights"));
    }

    private DashboardSummaryResponse response() {
        return new DashboardSummaryResponse(
                2,
                Map.of(
                        NomeStatusProjeto.PLANEJADO, 1L,
                        NomeStatusProjeto.EM_ANDAMENTO, 1L
                ),
                new BigDecimal("300.00"),
                new BigDecimal("450.00"),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("15.00"),
                new BigDecimal("10.00")
        );
    }

    private DashboardInsightResponse insightResponse(Long orientationId) {
        return new DashboardInsightResponse(
                orientationId == null
                        ? DashboardInsightScope.GLOBAL
                        : DashboardInsightScope.ORIENTATION,
                orientationId,
                response(),
                "O portfólio apresenta resultado estimado positivo.",
                List.of("O retorno supera o investimento."),
                List.of("Um projeto ainda está em andamento."),
                List.of("Acompanhar a realização do retorno."),
                Instant.parse("2026-09-16T15:00:00Z")
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
