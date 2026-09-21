package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.dto.dashboard.DashboardInsightResponse;
import br.com.fiap.aguiax.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiax.service.DashboardInsightService;
import br.com.fiap.aguiax.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIDER')")
@Tag(name = "Dashboard", description = "Indicadores executivos calculados pelo backend")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardInsightService dashboardInsightService;

    @GetMapping("/summary")
    @Operation(
            summary = "Consultar resumo executivo",
            description = "Somente LIDER. Sem orientationId retorna o resumo global; com o parâmetro, filtra pela orientação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo calculado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de LIDER"),
            @ApiResponse(responseCode = "404", description = "Orientação não encontrada")
    })
    public ResponseEntity<DashboardSummaryResponse> buscarResumo(
            @RequestParam(name = "orientationId", required = false) Long orientationId
    ) {
        return ResponseEntity.ok(dashboardService.buscarResumo(orientationId));
    }

    @PostMapping("/insights")
    @Operation(
            summary = "Gerar análise executiva com IA",
            description = "Somente LIDER. Analisa o resumo confiável do backend, sem alterar dados de negócio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise validada e armazenada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "O recorte não contém projetos"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de LIDER"),
            @ApiResponse(responseCode = "404", description = "Orientação não encontrada"),
            @ApiResponse(responseCode = "502", description = "Resposta rejeitada ou inválida do provedor de IA"),
            @ApiResponse(responseCode = "503", description = "IA, provedor ou MongoDB indisponível"),
            @ApiResponse(responseCode = "504", description = "Tempo limite excedido pelo provedor de IA")
    })
    public ResponseEntity<DashboardInsightResponse> gerarInsight(
            @RequestParam(name = "orientationId", required = false) Long orientationId
    ) {
        return ResponseEntity.ok(
                dashboardInsightService.gerarInsight(orientationId)
        );
    }
}
