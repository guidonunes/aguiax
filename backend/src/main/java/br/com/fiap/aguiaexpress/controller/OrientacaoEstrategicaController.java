package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.orientacao.MostrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.dto.orientacao.RegistrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.service.OrientacaoEstrategicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orientacoes")
@RequiredArgsConstructor
@Tag(name = "Orientações estratégicas", description = "Diretrizes estratégicas administradas por líderes")
public class OrientacaoEstrategicaController {

    private final OrientacaoEstrategicaService orientacaoService;

    @PostMapping
    @PreAuthorize("hasRole('LIDER')")
    @Operation(summary = "Criar orientação estratégica", description = "Somente LIDER.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orientação criada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de LIDER"),
            @ApiResponse(responseCode = "409", description = "Título de orientação duplicado")
    })
    public ResponseEntity<MostrarOrientacaoDTO> criarOrientacao(
            @RequestBody @Valid RegistrarOrientacaoDTO orientacaoDTO
    ) {
        MostrarOrientacaoDTO orientacaoCriada = orientacaoService.criarOrientacao(orientacaoDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orientacaoCriada);
    }

    @GetMapping
    public ResponseEntity<List<MostrarOrientacaoDTO>> listarOrientacoes() {
        List<MostrarOrientacaoDTO> orientacoes = orientacaoService.listarOrientacoes();

        return ResponseEntity.ok(orientacoes);
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<MostrarOrientacaoDTO>> listarOrientacoesAtivas() {
        List<MostrarOrientacaoDTO> orientacoes = orientacaoService.listarOrientacoesAtivas();

        return ResponseEntity.ok(orientacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarOrientacaoDTO> buscarOrientacaoPorId(
            @PathVariable Long id
    ) {
        MostrarOrientacaoDTO orientacao = orientacaoService.buscarOrientacaoPorId(id);

        return ResponseEntity.ok(orientacao);
    }

    @GetMapping("/area")
    public ResponseEntity<List<MostrarOrientacaoDTO>> buscarOrientacoesPorArea(
            @RequestParam String areaEstrategica
    ) {
        List<MostrarOrientacaoDTO> orientacoes =
                orientacaoService.buscarOrientacoesPorArea(areaEstrategica);

        return ResponseEntity.ok(orientacoes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    @Operation(summary = "Atualizar orientação estratégica", description = "Somente LIDER.")
    public ResponseEntity<MostrarOrientacaoDTO> atualizarOrientacao(
            @PathVariable Long id,
            @RequestBody @Valid RegistrarOrientacaoDTO orientacaoDTO
    ) {
        MostrarOrientacaoDTO orientacaoAtualizada =
                orientacaoService.atualizarOrientacao(id, orientacaoDTO);

        return ResponseEntity.ok(orientacaoAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    @Operation(
            summary = "Desativar orientação estratégica",
            description = "Somente LIDER. A exclusão é uma desativação lógica."
    )
    public ResponseEntity<Void> deletarOrientacao(
            @PathVariable Long id
    ) {
        orientacaoService.deletarOrientacao(id);

        return ResponseEntity.noContent().build();
    }
}
