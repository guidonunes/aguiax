package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.advice.ApplicationExceptionHandler;
import br.com.fiap.aguiax.dto.projeto.MostrarProjetoDTO;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.service.ProjetoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        ProjetoController.class,
        ApplicationExceptionHandler.class,
        ProjetoResultContractTest.TestSecurityConfiguration.class
})
class ProjetoResultContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjetoService projetoService;

    @Test
    void unauthenticatedCallerCannotInspectProjectResults() throws Exception {
        mockMvc.perform(get("/projetos/{id}", 42L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projetoService);
    }

    @Test
    void leaderCanInspectTheCompleteProjectResultContract() throws Exception {
        when(projetoService.buscarProjetoPorId(42L)).thenReturn(projectResponse());

        mockMvc.perform(get("/projetos/{id}", 42L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.ideiaId").value(7))
                .andExpect(jsonPath("$.tituloIdeia").value("Reduzir consumo de combustível"))
                .andExpect(jsonPath("$.usuarioId").value(17))
                .andExpect(jsonPath("$.nomeUsuarioResponsavel").value("Gestora responsável"))
                .andExpect(jsonPath("$.statusProjetoId").value(2))
                .andExpect(jsonPath("$.statusProjeto").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.titulo").value("Projeto de eficiência"))
                .andExpect(jsonPath("$.descricao").value("Executar melhorias na frota"))
                .andExpect(jsonPath("$.objetivo").value("Reduzir custos operacionais"))
                .andExpect(jsonPath("$.investimento").value(100000.00))
                .andExpect(jsonPath("$.roi").value(25.50))
                .andExpect(jsonPath("$.retornoFinanceiro").value(160000.00))
                .andExpect(jsonPath("$.reducaoCustos").value(15000.00))
                .andExpect(jsonPath("$.ganhoProdutividade").value(8.75))
                .andExpect(jsonPath("$.dataInicio").value("2026-01-10"))
                .andExpect(jsonPath("$.dataPrevisaoFim").value("2026-12-10"))
                .andExpect(jsonPath("$.dataConclusao").doesNotExist())
                .andExpect(jsonPath("$.dataCriacao").value("2026-01-05T09:30:00"))
                .andExpect(jsonPath("$.dataAtualizacao").value("2026-09-11T14:00:00"));

        verify(projetoService).buscarProjetoPorId(42L);
    }

    @Test
    void unknownProjectReturnsTheCentralized404Contract() throws Exception {
        when(projetoService.buscarProjetoPorId(999L))
                .thenThrow(new ResourceNotFoundException("Projeto não encontrado."));

        mockMvc.perform(get("/projetos/{id}", 999L)
                        .with(user("lider").roles("LIDER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Projeto não encontrado."))
                .andExpect(jsonPath("$.path").value("/projetos/999"));

        verify(projetoService).buscarProjetoPorId(999L);
    }

    private MostrarProjetoDTO projectResponse() {
        return new MostrarProjetoDTO(
                42L,
                7L,
                "Reduzir consumo de combustível",
                17L,
                "Gestora responsável",
                2L,
                NomeStatusProjeto.EM_ANDAMENTO,
                "Projeto de eficiência",
                "Executar melhorias na frota",
                "Reduzir custos operacionais",
                new BigDecimal("100000.00"),
                new BigDecimal("25.50"),
                new BigDecimal("160000.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("8.75"),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 12, 10),
                null,
                LocalDateTime.of(2026, 1, 5, 9, 30),
                LocalDateTime.of(2026, 9, 11, 14, 0)
        );
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
