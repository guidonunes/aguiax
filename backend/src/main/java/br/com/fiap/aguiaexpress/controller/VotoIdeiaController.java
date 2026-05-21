package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.MostrarVotoIdeiaDTO;
import br.com.fiap.aguiaexpress.service.VotoIdeiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/votos-ideias")
@RequiredArgsConstructor
public class VotoIdeiaController {

    private final VotoIdeiaService votoIdeiaService;

    @PostMapping("/usuario/{usuarioId}/ideia/{ideiaId}")
    public ResponseEntity<MostrarVotoIdeiaDTO> votarIdeia(
            @PathVariable Long usuarioId,
            @PathVariable Long ideiaId
    ) {
        MostrarVotoIdeiaDTO voto = votoIdeiaService.votarIdeia(usuarioId, ideiaId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(voto);
    }

    @GetMapping
    public ResponseEntity<List<MostrarVotoIdeiaDTO>> listarVotos() {
        List<MostrarVotoIdeiaDTO> votos = votoIdeiaService.listarVotos();

        return ResponseEntity.ok(votos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarVotoIdeiaDTO> buscarVotoPorId(
            @PathVariable Long id
    ) {
        MostrarVotoIdeiaDTO voto = votoIdeiaService.buscarVotoPorId(id);

        return ResponseEntity.ok(voto);
    }

    @GetMapping("/ideia/{ideiaId}")
    public ResponseEntity<List<MostrarVotoIdeiaDTO>> listarVotosPorIdeia(
            @PathVariable Long ideiaId
    ) {
        List<MostrarVotoIdeiaDTO> votos = votoIdeiaService.listarVotosPorIdeia(ideiaId);

        return ResponseEntity.ok(votos);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MostrarVotoIdeiaDTO>> listarVotosPorUsuario(
            @PathVariable Long usuarioId
    ) {
        List<MostrarVotoIdeiaDTO> votos = votoIdeiaService.listarVotosPorUsuario(usuarioId);

        return ResponseEntity.ok(votos);
    }

    @DeleteMapping("/usuario/{usuarioId}/ideia/{ideiaId}")
    public ResponseEntity<Void> removerVoto(
            @PathVariable Long usuarioId,
            @PathVariable Long ideiaId
    ) {
        votoIdeiaService.removerVoto(usuarioId, ideiaId);

        return ResponseEntity.noContent().build();
    }
}