package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.orientacao.MostrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.dto.orientacao.RegistrarOrientacaoDTO;
import br.com.fiap.aguiaexpress.service.OrientacaoEstrategicaService;
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
public class OrientacaoEstrategicaController {

    private final OrientacaoEstrategicaService orientacaoService;

    @PostMapping
    @PreAuthorize("hasRole('LIDER')")
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
    public ResponseEntity<Void> deletarOrientacao(
            @PathVariable Long id
    ) {
        orientacaoService.deletarOrientacao(id);

        return ResponseEntity.noContent().build();
    }
}