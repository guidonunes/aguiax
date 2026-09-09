package br.com.fiap.aguiaexpress.controller;


import br.com.fiap.aguiaexpress.dto.AtualizarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.MostrarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarUsuarioDTO;
import br.com.fiap.aguiaexpress.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

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
    public ResponseEntity<List<MostrarUsuarioDTO>> listarTodosUsuarios() {
        List<MostrarUsuarioDTO> usuarios = usuarioService.listarTodosUsuarios();

        return ResponseEntity.ok(usuarios);
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
    public ResponseEntity<Void> deletarUsuarioPorId(
            @PathVariable Long id
    ) {
        usuarioService.deletarUsuarioPorId(id);

        return ResponseEntity.noContent().build();
    }
}
