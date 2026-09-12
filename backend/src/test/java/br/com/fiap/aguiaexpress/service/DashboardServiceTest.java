package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.Projeto;
import br.com.fiap.aguiaexpress.model.StatusProjeto;
import br.com.fiap.aguiaexpress.repository.OrientacaoEstrategicaRepository;
import br.com.fiap.aguiaexpress.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private ProjetoRepository projetoRepository;
    private OrientacaoEstrategicaRepository orientacaoEstrategicaRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        projetoRepository = mock(ProjetoRepository.class);
        orientacaoEstrategicaRepository = mock(OrientacaoEstrategicaRepository.class);
        dashboardService = new DashboardService(
                projetoRepository,
                orientacaoEstrategicaRepository
        );
    }

    @Test
    void shouldAggregateEveryGlobalDashboardMetric() {
        when(projetoRepository.findAll()).thenReturn(List.of(
                projeto(NomeStatusProjeto.PLANEJADO, "100.00", "10.00", "150.00", "20.00", "5.00"),
                projeto(NomeStatusProjeto.EM_ANDAMENTO, "200.00", "20.00", "250.00", "30.00", "15.00"),
                projeto(NomeStatusProjeto.EM_ANDAMENTO, "300.00", "30.00", "350.00", "40.00", "25.00"),
                projeto(NomeStatusProjeto.CONCLUIDO, "400.00", "40.00", "450.00", "50.00", "35.00")
        ));

        DashboardSummaryResponse response = dashboardService.buscarResumo(null);

        assertEquals(4, response.totalProjects());
        assertEquals(1L, response.projectsByStatus().get(NomeStatusProjeto.PLANEJADO));
        assertEquals(2L, response.projectsByStatus().get(NomeStatusProjeto.EM_ANDAMENTO));
        assertEquals(1L, response.projectsByStatus().get(NomeStatusProjeto.CONCLUIDO));
        assertEquals(0L, response.projectsByStatus().get(NomeStatusProjeto.CANCELADO));
        assertDecimalEquals("1000.00", response.totalInvestment());
        assertDecimalEquals("1200.00", response.totalFinancialReturn());
        assertDecimalEquals("140.00", response.totalCostReduction());
        assertDecimalEquals("340.00", response.estimatedResult());
        assertDecimalEquals("25.00", response.averageRoi());
        assertDecimalEquals("20.00", response.averageProductivityGain());
        verify(projetoRepository).findAll();
    }

    @Test
    void shouldReturnACompleteZeroSummaryWhenThereAreNoProjects() {
        when(projetoRepository.findAll()).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.buscarResumo(null);

        assertEquals(0, response.totalProjects());
        for (NomeStatusProjeto status : NomeStatusProjeto.values()) {
            assertEquals(0L, response.projectsByStatus().get(status));
        }
        assertDecimalEquals("0", response.totalInvestment());
        assertDecimalEquals("0", response.totalFinancialReturn());
        assertDecimalEquals("0", response.totalCostReduction());
        assertDecimalEquals("0", response.estimatedResult());
        assertDecimalEquals("0", response.averageRoi());
        assertDecimalEquals("0", response.averageProductivityGain());
    }

    @Test
    void shouldTreatLegacyNullMetricsAsZeroAndRoundAveragesHalfUp() {
        Projeto projetoSemMetricas = projeto(
                NomeStatusProjeto.CANCELADO,
                null,
                null,
                null,
                null,
                null
        );
        when(projetoRepository.findAll()).thenReturn(List.of(
                projeto(NomeStatusProjeto.PLANEJADO, "10.00", "10.00", "4.00", "1.00", "2.00"),
                projeto(NomeStatusProjeto.CONCLUIDO, "20.00", "0.00", "8.00", "2.00", "0.00"),
                projetoSemMetricas
        ));

        DashboardSummaryResponse response = dashboardService.buscarResumo(null);

        assertDecimalEquals("30.00", response.totalInvestment());
        assertDecimalEquals("12.00", response.totalFinancialReturn());
        assertDecimalEquals("3.00", response.totalCostReduction());
        assertDecimalEquals("-15.00", response.estimatedResult());
        assertDecimalEquals("3.33", response.averageRoi());
        assertDecimalEquals("0.67", response.averageProductivityGain());
    }

    @Test
    void shouldAggregateOnlyProjectsFromTheRequestedOrientation() {
        when(orientacaoEstrategicaRepository.existsById(42L)).thenReturn(true);
        when(projetoRepository.findByIdeia_OrientacaoEstrategica_Id(42L)).thenReturn(List.of(
                projeto(NomeStatusProjeto.EM_ANDAMENTO, "100.00", "10.00", "180.00", "20.00", "8.00"),
                projeto(NomeStatusProjeto.CONCLUIDO, "200.00", "30.00", "320.00", "40.00", "12.00")
        ));

        DashboardSummaryResponse response = dashboardService.buscarResumo(42L);

        assertEquals(2, response.totalProjects());
        assertEquals(1L, response.projectsByStatus().get(NomeStatusProjeto.EM_ANDAMENTO));
        assertEquals(1L, response.projectsByStatus().get(NomeStatusProjeto.CONCLUIDO));
        assertDecimalEquals("300.00", response.totalInvestment());
        assertDecimalEquals("500.00", response.totalFinancialReturn());
        assertDecimalEquals("60.00", response.totalCostReduction());
        assertDecimalEquals("260.00", response.estimatedResult());
        assertDecimalEquals("20.00", response.averageRoi());
        assertDecimalEquals("10.00", response.averageProductivityGain());
        verify(orientacaoEstrategicaRepository).existsById(42L);
        verify(projetoRepository).findByIdeia_OrientacaoEstrategica_Id(42L);
    }

    @Test
    void shouldReturnAZeroSummaryForAnExistingOrientationWithoutProjects() {
        when(orientacaoEstrategicaRepository.existsById(42L)).thenReturn(true);
        when(projetoRepository.findByIdeia_OrientacaoEstrategica_Id(42L))
                .thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.buscarResumo(42L);

        assertEquals(0, response.totalProjects());
        assertDecimalEquals("0", response.estimatedResult());
        verify(projetoRepository).findByIdeia_OrientacaoEstrategica_Id(42L);
    }

    @Test
    void shouldRejectAnUnknownOrientationBeforeQueryingProjects() {
        when(orientacaoEstrategicaRepository.existsById(404L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dashboardService.buscarResumo(404L)
        );

        assertEquals("Orientação estratégica não encontrada.", exception.getMessage());
        verify(orientacaoEstrategicaRepository).existsById(404L);
        verifyNoInteractions(projetoRepository);
    }

    private Projeto projeto(
            NomeStatusProjeto status,
            String investimento,
            String roi,
            String retornoFinanceiro,
            String reducaoCustos,
            String ganhoProdutividade
    ) {
        Projeto projeto = new Projeto();
        projeto.setStatusProjeto(new StatusProjeto(1L, status, status.name()));
        projeto.setInvestimento(decimal(investimento));
        projeto.setRoi(decimal(roi));
        projeto.setRetornoFinanceiro(decimal(retornoFinanceiro));
        projeto.setReducaoCustos(decimal(reducaoCustos));
        projeto.setGanhoProdutividade(decimal(ganhoProdutividade));
        return projeto;
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
