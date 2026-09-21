package br.com.fiap.aguiax.controller;


import br.com.fiap.aguiax.dto.usuario.AtualizarUsuarioDTO;
import br.com.fiap.aguiax.dto.usuario.MostrarUsuarioDTO;
import br.com.fiap.aguiax.dto.usuario.RegistrarUsuarioDTO;
import br.com.fiap.aguiax.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro público e administração de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/admin")
    @PreAuthorize("hasRole('LIDER')")
    @Operation(
            summary = "Criar usuário como líder",
            description = "Somente LIDER. Provisiona uma conta com um dos perfis existentes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de LIDER"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<MostrarUsuarioDTO> criarUsuarioAdmin(
            @RequestBody @Valid RegistrarUsuarioDTO usuarioDTO
    ) {
        MostrarUsuarioDTO usuarioCriado =
                usuarioService.salvarUsuarioAdmin(usuarioDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioCriado);
    }

    @PostMapping
    @SecurityRequirements
    @Operation(
            summary = "Cadastrar usuário operacional",
            description = "Rota pública. Sempre cria OPERACIONAL e rejeita perfis privilegiados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário operacional criado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tentativa de perfil privilegiado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<MostrarUsuarioDTO> salvarUsuario(
            @RequestBody @Valid RegistrarUsuarioDTO usuarioDTO
    ) {
        MostrarUsuarioDTO usuarioSalvo = usuarioService.salvarUsuario(usuarioDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioSalvo);
    }

    @GetMapping
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<List<MostrarUsuarioDTO>> listarTodosUsuarios() {
        return ResponseEntity.ok(
                usuarioService.listarTodosUsuarios()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarUsuarioDTO> buscarUsuarioPorId(
            @PathVariable Long id
    ) {
        MostrarUsuarioDTO usuario = usuarioService.buscarUsuarioPorId(id);

        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MostrarUsuarioDTO> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarUsuarioDTO usuarioDTO
    ) {
        MostrarUsuarioDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, usuarioDTO);

        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<Void> deletarUsuarioPorId(
            @PathVariable Long id
    ) {
        usuarioService.deletarUsuarioPorId(id);

        return ResponseEntity.noContent().build();
    }
}
