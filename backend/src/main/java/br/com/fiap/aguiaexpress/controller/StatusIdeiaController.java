package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.ideia.MostrarStatusIdeiaDTO;
import br.com.fiap.aguiaexpress.service.StatusIdeiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status-ideias")
@RequiredArgsConstructor
public class StatusIdeiaController {

    private final StatusIdeiaService statusIdeiaService;

    @GetMapping
    public ResponseEntity<List<MostrarStatusIdeiaDTO>> listarStatusIdeias() {
        List<MostrarStatusIdeiaDTO> statusIdeias = statusIdeiaService.listarStatusIdeias();

        return ResponseEntity.ok(statusIdeias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarStatusIdeiaDTO> buscarStatusIdeiaPorId(
            @PathVariable Long id
    ) {
        MostrarStatusIdeiaDTO statusIdeia = statusIdeiaService.buscarStatusIdeiaPorId(id);

        return ResponseEntity.ok(statusIdeia);
    }
}