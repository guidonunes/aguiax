package br.com.fiap.aguiaexpress.controller;


import br.com.fiap.aguiaexpress.dto.AtualizarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.MostrarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarUsuarioDTO;
import br.com.fiap.aguiaexpress.service.UsuarioService;
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
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/admin")
    @PreAuthorize("hasRole('LIDER')")
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
