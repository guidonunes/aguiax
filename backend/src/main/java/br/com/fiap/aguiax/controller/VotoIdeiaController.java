package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.dto.ideia.MostrarVotoIdeiaDTO;
import br.com.fiap.aguiax.service.VotoIdeiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/votos-ideias")
@RequiredArgsConstructor
@Tag(name = "Votos", description = "Votos autenticados em ideias")
public class VotoIdeiaController {

    private final VotoIdeiaService votoIdeiaService;

    @PostMapping("/ideia/{ideiaId}")
    @PreAuthorize("hasAnyRole('OPERACIONAL', 'GESTOR', 'LIDER')")
    @Operation(
            summary = "Votar em uma ideia",
            description = "Qualquer perfil autenticado. O autor do voto vem do JWT e só pode votar uma vez por ideia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voto criado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Ideia não encontrada"),
            @ApiResponse(responseCode = "409", description = "Usuário já votou nessa ideia"),
            @ApiResponse(responseCode = "503", description = "Prioridade temporariamente indisponível")
    })
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
    @Operation(
            summary = "Remover o próprio voto",
            description = "Remove apenas o voto do usuário identificado pelo JWT e recalcula a prioridade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Voto removido"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Ideia ou voto próprio não encontrado"),
            @ApiResponse(responseCode = "503", description = "Prioridade temporariamente indisponível")
    })
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
