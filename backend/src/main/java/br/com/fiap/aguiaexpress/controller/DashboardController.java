package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.dashboard.DashboardSummaryResponse;
import br.com.fiap.aguiaexpress.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam(required = false) Long orientationId
    ) {
        return ResponseEntity.ok(dashboardService.buscarResumo(orientationId));
    }
}
