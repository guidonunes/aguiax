package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.AtualizarStatusProjetoDTO;
import br.com.fiap.aguiaexpress.dto.MostrarProjetoDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarProjetoDTO;
import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.service.ProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MostrarProjetoDTO> criarProjeto(
            @RequestBody @Valid RegistrarProjetoDTO projetoDTO
    ) {
        MostrarProjetoDTO projetoCriado = projetoService.criarProjeto(projetoDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projetoCriado);
    }

    @GetMapping
    public ResponseEntity<List<MostrarProjetoDTO>> listarProjetos() {
        List<MostrarProjetoDTO> projetos = projetoService.listarProjetos();

        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarProjetoDTO> buscarProjetoPorId(
            @PathVariable Long id
    ) {
        MostrarProjetoDTO projeto = projetoService.buscarProjetoPorId(id);

        return ResponseEntity.ok(projeto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MostrarProjetoDTO> atualizarProjeto(
            @PathVariable Long id,
            @RequestBody @Valid RegistrarProjetoDTO projetoDTO
    ) {
        MostrarProjetoDTO projetoAtualizado = projetoService.atualizarProjeto(id, projetoDTO);

        return ResponseEntity.ok(projetoAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletarProjeto(
            @PathVariable Long id
    ) {
        projetoService.deletarProjeto(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MostrarProjetoDTO>> listarProjetosPorUsuario(
            @PathVariable Long usuarioId
    ) {
        List<MostrarProjetoDTO> projetos = projetoService.listarProjetosPorUsuario(usuarioId);

        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MostrarProjetoDTO>> listarProjetosPorStatus(
            @PathVariable NomeStatusProjeto status
    ) {
        List<MostrarProjetoDTO> projetos = projetoService.listarProjetosPorStatus(status);

        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<MostrarProjetoDTO>> buscarProjetosPorTitulo(
            @RequestParam String titulo
    ) {
        List<MostrarProjetoDTO> projetos = projetoService.buscarProjetosPorTitulo(titulo);

        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/ideia/{ideiaId}")
    public ResponseEntity<MostrarProjetoDTO> buscarProjetoPorIdeia(
            @PathVariable Long ideiaId
    ) {
        MostrarProjetoDTO projeto = projetoService.buscarProjetoPorIdeia(ideiaId);

        return ResponseEntity.ok(projeto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MostrarProjetoDTO> atualizarStatusProjeto(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusProjetoDTO statusDTO
    ) {
        MostrarProjetoDTO projetoAtualizado = projetoService.atualizarStatusProjeto(id, statusDTO);

        return ResponseEntity.ok(projetoAtualizado);
    }
}