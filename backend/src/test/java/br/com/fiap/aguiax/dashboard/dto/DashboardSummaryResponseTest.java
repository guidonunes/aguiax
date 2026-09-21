package br.com.fiap.aguiax.dashboard.dto;

import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardSummaryResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeTheStableDashboardContract() throws Exception {
        DashboardSummaryResponse response = new DashboardSummaryResponse(
                12,
                Map.of(
                        NomeStatusProjeto.PLANEJADO, 3L,
                        NomeStatusProjeto.EM_ANDAMENTO, 5L,
                        NomeStatusProjeto.CONCLUIDO, 3L,
                        NomeStatusProjeto.CANCELADO, 1L
                ),
                new BigDecimal("850000.00"),
                new BigDecimal("1200000.00"),
                new BigDecimal("210000.00"),
                new BigDecimal("560000.00"),
                new BigDecimal("32.7"),
                new BigDecimal("11.4")
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertEquals(8, json.size());
        assertEquals(12, json.get("totalProjects").longValue());
        assertEquals(4, json.get("projectsByStatus").size());
        assertEquals(3, json.get("projectsByStatus").get("PLANEJADO").longValue());
        assertEquals(5, json.get("projectsByStatus").get("EM_ANDAMENTO").longValue());
        assertEquals(3, json.get("projectsByStatus").get("CONCLUIDO").longValue());
        assertEquals(1, json.get("projectsByStatus").get("CANCELADO").longValue());
        assertEquals(0, json.get("totalInvestment").decimalValue().compareTo(new BigDecimal("850000.00")));
        assertEquals(0, json.get("totalFinancialReturn").decimalValue().compareTo(new BigDecimal("1200000.00")));
        assertEquals(0, json.get("totalCostReduction").decimalValue().compareTo(new BigDecimal("210000.00")));
        assertEquals(0, json.get("estimatedResult").decimalValue().compareTo(new BigDecimal("560000.00")));
        assertEquals(0, json.get("averageRoi").decimalValue().compareTo(new BigDecimal("32.7")));
        assertEquals(0, json.get("averageProductivityGain").decimalValue().compareTo(new BigDecimal("11.4")));
    }

    @Test
    void shouldZeroFillMissingStatusesAndDefensivelyCopyTheMap() {
        EnumMap<NomeStatusProjeto, Long> source = new EnumMap<>(NomeStatusProjeto.class);
        source.put(NomeStatusProjeto.EM_ANDAMENTO, 2L);

        DashboardSummaryResponse response = response(2, source);
        source.put(NomeStatusProjeto.EM_ANDAMENTO, 99L);

        assertEquals(4, response.projectsByStatus().size());
        assertEquals(0L, response.projectsByStatus().get(NomeStatusProjeto.PLANEJADO));
        assertEquals(2L, response.projectsByStatus().get(NomeStatusProjeto.EM_ANDAMENTO));
        assertEquals(0L, response.projectsByStatus().get(NomeStatusProjeto.CONCLUIDO));
        assertEquals(0L, response.projectsByStatus().get(NomeStatusProjeto.CANCELADO));
        assertThrows(
                UnsupportedOperationException.class,
                () -> response.projectsByStatus().put(NomeStatusProjeto.PLANEJADO, 1L)
        );
    }

    @Test
    void shouldRejectAStatusTotalThatDoesNotMatchTheProjectTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> response(3, Map.of(NomeStatusProjeto.EM_ANDAMENTO, 2L))
        );
    }

    @Test
    void shouldRejectNegativeCountsAndMissingDecimalMetrics() {
        assertThrows(
                IllegalArgumentException.class,
                () -> response(0, Map.of(NomeStatusProjeto.PLANEJADO, -1L))
        );

        assertThrows(
                NullPointerException.class,
                () -> new DashboardSummaryResponse(
                        0,
                        Map.of(),
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    private DashboardSummaryResponse response(
            long totalProjects,
            Map<NomeStatusProjeto, Long> projectsByStatus
    ) {
        return new DashboardSummaryResponse(
                totalProjects,
                projectsByStatus,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
