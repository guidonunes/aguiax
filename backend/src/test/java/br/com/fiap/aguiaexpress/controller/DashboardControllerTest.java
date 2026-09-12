package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
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
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
