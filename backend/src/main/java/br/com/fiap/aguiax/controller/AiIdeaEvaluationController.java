package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.dto.ai.AiIdeaEvaluationResponse;
import br.com.fiap.aguiax.service.AiIdeaEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
@Tag(name = "Avaliação de IA", description = "Análise consultiva e estruturada de ideias")
public class AiIdeaEvaluationController {

    private final AiIdeaEvaluationService aiIdeaEvaluationService;

    @PostMapping("/{ideaId}/ai-evaluation")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(
            summary = "Avaliar ideia com IA",
            description = "Somente GESTOR. Persiste o histórico no MongoDB e recalcula a prioridade; nunca altera o status da ideia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação estruturada concluída", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de GESTOR"),
            @ApiResponse(responseCode = "404", description = "Ideia não encontrada"),
            @ApiResponse(responseCode = "502", description = "Resposta inválida ou rejeitada pelo provedor"),
            @ApiResponse(responseCode = "503", description = "IA, histórico ou prioridade temporariamente indisponível"),
            @ApiResponse(responseCode = "504", description = "Tempo limite do provedor excedido")
    })
    public ResponseEntity<AiIdeaEvaluationResponse> avaliarIdeia(
            @PathVariable("ideaId") Long ideaId
    ) {
        return ResponseEntity.ok(
                aiIdeaEvaluationService.avaliarIdeia(ideaId)
        );
    }
}
