package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.dto.ideia.AtualizarAvaliacaoIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.MostrarIdeiaDTO;
import br.com.fiap.aguiax.dto.ideia.RegistrarIdeiaDTO;
import br.com.fiap.aguiax.model.NomeStatusIdeia;
import br.com.fiap.aguiax.service.IdeiaService;
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
@RequestMapping("/ideias")
@RequiredArgsConstructor
@Tag(name = "Ideias", description = "Cadastro, consulta, avaliação gerencial e fluxo de ideias")
public class IdeiaController {

    private final IdeiaService ideiaService;

    @PostMapping
    @PreAuthorize("hasRole('OPERACIONAL')")
    @Operation(
            summary = "Criar ideia",
            description = "Somente OPERACIONAL. O autor é obtido do JWT e a ideia começa em NOVA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ideia criada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de OPERACIONAL"),
            @ApiResponse(responseCode = "404", description = "Categoria ou orientação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Título de ideia duplicado")
    })
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
    @Operation(
            summary = "Atualizar ideia própria",
            description = "Somente o OPERACIONAL que criou a ideia pode alterá-la."
    )
    public ResponseEntity<MostrarIdeiaDTO> atualizarIdeia(
            @PathVariable Long id,
            @RequestBody @Valid RegistrarIdeiaDTO ideiaDTO
    ) {
        MostrarIdeiaDTO ideiaAtualizada = ideiaService.atualizarIdeia(id, ideiaDTO);

        return ResponseEntity.ok(ideiaAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERACIONAL')")
    @Operation(
            summary = "Excluir ideia própria",
            description = "Somente o OPERACIONAL que criou a ideia pode excluí-la."
    )
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
    @Operation(
            summary = "Alterar status da ideia",
            description = "Somente GESTOR. Aceita apenas transições do fluxo documentado; a conversão em projeto ocorre em POST /projetos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Status ausente ou transição inválida"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de GESTOR"),
            @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    })
    public ResponseEntity<MostrarIdeiaDTO> atualizarStatusIdeia(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusIdeiaDTO statusDTO
    ) {
        MostrarIdeiaDTO ideiaAtualizada = ideiaService.atualizarStatusIdeia(id, statusDTO);

        return ResponseEntity.ok(ideiaAtualizada);
    }

    @PatchMapping("/{id}/avaliacao")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(
            summary = "Registrar avaliação gerencial",
            description = "Somente GESTOR. Impacto e viabilidade devem estar entre 0 e 10; a prioridade é recalculada sem alterar o status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação salva e prioridade recalculada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Pontuações ausentes ou fora de 0 a 10"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de GESTOR"),
            @ApiResponse(responseCode = "404", description = "Ideia não encontrada"),
            @ApiResponse(responseCode = "503", description = "Prioridade temporariamente indisponível")
    })
    public ResponseEntity<MostrarIdeiaDTO> atualizarAvaliacaoIdeia(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarAvaliacaoIdeiaDTO avaliacaoDTO
    ) {
        MostrarIdeiaDTO ideiaAtualizada =
                ideiaService.atualizarAvaliacaoIdeia(id, avaliacaoDTO);

        return ResponseEntity.ok(ideiaAtualizada);
    }
}
