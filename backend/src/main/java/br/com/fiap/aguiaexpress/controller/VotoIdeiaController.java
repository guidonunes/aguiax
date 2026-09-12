package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.ideia.MostrarVotoIdeiaDTO;
import br.com.fiap.aguiaexpress.service.VotoIdeiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/votos-ideias")
@RequiredArgsConstructor
public class VotoIdeiaController {

    private final VotoIdeiaService votoIdeiaService;

    @PostMapping("/ideia/{ideiaId}")
    @PreAuthorize("hasAnyRole('OPERACIONAL', 'GESTOR', 'LIDER')")
    public ResponseEntity<MostrarVotoIdeiaDTO> votarIdeia(
            @PathVariable Long ideiaId
    ) {
        MostrarVotoIdeiaDTO voto = votoIdeiaService.votarIdeia(ideiaId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(voto);
    }

    @DeleteMapping("/ideia/{ideiaId}")
    @PreAuthorize("hasAnyRole('OPERACIONAL', 'GESTOR', 'LIDER')")
    public ResponseEntity<Void> removerVoto(
            @PathVariable Long ideiaId
    ) {
        votoIdeiaService.removerVoto(ideiaId);

        return ResponseEntity.noContent().build();
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
}