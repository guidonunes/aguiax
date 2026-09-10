package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.AtualizarAvaliacaoIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.MostrarIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarIdeiaDTO;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.service.IdeiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ideias")
@RequiredArgsConstructor
public class IdeiaController {

    private final IdeiaService ideiaService;

    @PostMapping
    @PreAuthorize("hasRole('OPERACIONAL')")
    public ResponseEntity<MostrarIdeiaDTO> criarIdeia(
            @RequestBody @Valid RegistrarIdeiaDTO ideiaDTO
    ) {
        MostrarIdeiaDTO ideiaCriada = ideiaService.criarIdeia(ideiaDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ideiaCriada);
    }

    @GetMapping
    public ResponseEntity<List<MostrarIdeiaDTO>> listarIdeias() {
        List<MostrarIdeiaDTO> ideias = ideiaService.listarIdeias();

        return ResponseEntity.ok(ideias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarIdeiaDTO> buscarIdeiaPorId(
            @PathVariable Long id
    ) {
        MostrarIdeiaDTO ideia = ideiaService.buscarIdeiaPorId(id);

        return ResponseEntity.ok(ideia);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERACIONAL')")
    public ResponseEntity<MostrarIdeiaDTO> atualizarIdeia(
            @PathVariable Long id,
            @RequestBody @Valid RegistrarIdeiaDTO ideiaDTO
    ) {
        MostrarIdeiaDTO ideiaAtualizada = ideiaService.atualizarIdeia(id, ideiaDTO);

        return ResponseEntity.ok(ideiaAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERACIONAL')")
    public ResponseEntity<Void> deletarIdeia(
            @PathVariable Long id
    ) {
        ideiaService.deletarIdeia(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MostrarIdeiaDTO>> listarIdeiasPorUsuario(
            @PathVariable Long usuarioId
    ) {
        List<MostrarIdeiaDTO> ideias = ideiaService.listarIdeiasPorUsuario(usuarioId);

        return ResponseEntity.ok(ideias);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<MostrarIdeiaDTO>> listarIdeiasPorCategoria(
            @PathVariable Long categoriaId
    ) {
        List<MostrarIdeiaDTO> ideias = ideiaService.listarIdeiasPorCategoria(categoriaId);

        return ResponseEntity.ok(ideias);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MostrarIdeiaDTO>> listarIdeiasPorStatus(
            @PathVariable NomeStatusIdeia status
    ) {
        List<MostrarIdeiaDTO> ideias = ideiaService.listarIdeiasPorStatus(status);

        return ResponseEntity.ok(ideias);
    }

    @GetMapping("/orientacao/{orientacaoId}")
    public ResponseEntity<List<MostrarIdeiaDTO>> listarIdeiasPorOrientacao(
            @PathVariable Long orientacaoId
    ) {
        List<MostrarIdeiaDTO> ideias = ideiaService.listarIdeiasPorOrientacao(orientacaoId);

        return ResponseEntity.ok(ideias);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<MostrarIdeiaDTO>> buscarIdeiasPorTitulo(
            @RequestParam String titulo
    ) {
        List<MostrarIdeiaDTO> ideias = ideiaService.buscarIdeiasPorTitulo(titulo);

        return ResponseEntity.ok(ideias);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MostrarIdeiaDTO> atualizarStatusIdeia(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusIdeiaDTO statusDTO
    ) {
        MostrarIdeiaDTO ideiaAtualizada = ideiaService.atualizarStatusIdeia(id, statusDTO);

        return ResponseEntity.ok(ideiaAtualizada);
    }

    @PatchMapping("/{id}/avaliacao")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MostrarIdeiaDTO> atualizarAvaliacaoIdeia(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarAvaliacaoIdeiaDTO avaliacaoDTO
    ) {
        MostrarIdeiaDTO ideiaAtualizada =
                ideiaService.atualizarAvaliacaoIdeia(id, avaliacaoDTO);

        return ResponseEntity.ok(ideiaAtualizada);
    }
}
