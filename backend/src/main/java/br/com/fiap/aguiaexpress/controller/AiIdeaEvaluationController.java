package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.ai.AiIdeaEvaluationResponse;
import br.com.fiap.aguiaexpress.service.AiIdeaEvaluationService;
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
public class AiIdeaEvaluationController {

    private final AiIdeaEvaluationService aiIdeaEvaluationService;

    @PostMapping("/{ideaId}/ai-evaluation")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<AiIdeaEvaluationResponse> avaliarIdeia(
            @PathVariable Long ideaId
    ) {
        return ResponseEntity.ok(
                aiIdeaEvaluationService.avaliarIdeia(ideaId)
        );
    }
}
