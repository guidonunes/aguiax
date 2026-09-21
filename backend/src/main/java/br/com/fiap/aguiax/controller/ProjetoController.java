package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.dto.projeto.AtualizarStatusProjetoDTO;
import br.com.fiap.aguiax.dto.projeto.MostrarProjetoDTO;
import br.com.fiap.aguiax.dto.projeto.RegistrarProjetoDTO;
import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.service.ProjetoService;
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
@RequestMapping("/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Conversão de ideias aprovadas e gestão de projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(
            summary = "Criar projeto a partir de ideia aprovada",
            description = "Somente GESTOR. Aceita uma ideia APROVADA sem projeto e a muda transacionalmente para CONVERTIDA_EM_PROJETO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado e ideia convertida", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Dados financeiros inválidos ou ideia inelegível"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de GESTOR"),
            @ApiResponse(responseCode = "404", description = "Ideia, responsável ou status não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe um projeto para a ideia")
    })
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
    @Operation(
            summary = "Alterar status do projeto",
            description = "Somente GESTOR. Atualiza o status e registra a data quando o projeto é concluído."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Status ausente ou inválido"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de GESTOR"),
            @ApiResponse(responseCode = "404", description = "Projeto ou status não encontrado")
    })
    public ResponseEntity<MostrarProjetoDTO> atualizarStatusProjeto(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusProjetoDTO statusDTO
    ) {
        MostrarProjetoDTO projetoAtualizado = projetoService.atualizarStatusProjeto(id, statusDTO);

        return ResponseEntity.ok(projetoAtualizado);
    }
}
