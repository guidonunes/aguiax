package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.projeto.MostrarStatusProjetoDTO;
import br.com.fiap.aguiaexpress.service.StatusProjetoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status-projetos")
@RequiredArgsConstructor
public class StatusProjetoController {

    private final StatusProjetoService statusProjetoService;

    @GetMapping
    public ResponseEntity<List<MostrarStatusProjetoDTO>> listarStatusProjetos() {
        List<MostrarStatusProjetoDTO> statusProjetos = statusProjetoService.listarStatusProjetos();

        return ResponseEntity.ok(statusProjetos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarStatusProjetoDTO> buscarStatusProjetoPorId(
            @PathVariable Long id
    ) {
        MostrarStatusProjetoDTO statusProjeto = statusProjetoService.buscarStatusProjetoPorId(id);

        return ResponseEntity.ok(statusProjeto);
    }
}