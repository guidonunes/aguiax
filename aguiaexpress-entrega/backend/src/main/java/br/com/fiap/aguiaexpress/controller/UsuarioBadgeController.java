package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.MostrarUsuarioBadgeDTO;
import br.com.fiap.aguiaexpress.service.UsuarioBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario-badges")
@RequiredArgsConstructor
public class UsuarioBadgeController {

    private final UsuarioBadgeService usuarioBadgeService;

    @PostMapping("/usuario/{usuarioId}/badge/{badgeId}")
    public ResponseEntity<MostrarUsuarioBadgeDTO> concederBadge(
            @PathVariable Long usuarioId,
            @PathVariable Long badgeId
    ) {
        MostrarUsuarioBadgeDTO usuarioBadge =
                usuarioBadgeService.concederBadge(usuarioId, badgeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioBadge);
    }

    @PostMapping("/usuario/{usuarioId}/nome/{nomeBadge}")
    public ResponseEntity<MostrarUsuarioBadgeDTO> concederBadgePorNome(
            @PathVariable Long usuarioId,
            @PathVariable String nomeBadge
    ) {
        MostrarUsuarioBadgeDTO usuarioBadge =
                usuarioBadgeService.concederBadgePorNome(usuarioId, nomeBadge);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioBadge);
    }

    @GetMapping
    public ResponseEntity<List<MostrarUsuarioBadgeDTO>> listarUsuarioBadges() {
        List<MostrarUsuarioBadgeDTO> usuarioBadges =
                usuarioBadgeService.listarUsuarioBadges();

        return ResponseEntity.ok(usuarioBadges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarUsuarioBadgeDTO> buscarUsuarioBadgePorId(
            @PathVariable Long id
    ) {
        MostrarUsuarioBadgeDTO usuarioBadge =
                usuarioBadgeService.buscarUsuarioBadgePorId(id);

        return ResponseEntity.ok(usuarioBadge);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MostrarUsuarioBadgeDTO>> listarBadgesPorUsuario(
            @PathVariable Long usuarioId
    ) {
        List<MostrarUsuarioBadgeDTO> badges =
                usuarioBadgeService.listarBadgesPorUsuario(usuarioId);

        return ResponseEntity.ok(badges);
    }

    @GetMapping("/badge/{badgeId}")
    public ResponseEntity<List<MostrarUsuarioBadgeDTO>> listarUsuariosPorBadge(
            @PathVariable Long badgeId
    ) {
        List<MostrarUsuarioBadgeDTO> usuarios =
                usuarioBadgeService.listarUsuariosPorBadge(badgeId);

        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/usuario/{usuarioId}/badge/{badgeId}")
    public ResponseEntity<Void> removerBadgeDoUsuario(
            @PathVariable Long usuarioId,
            @PathVariable Long badgeId
    ) {
        usuarioBadgeService.removerBadgeDoUsuario(usuarioId, badgeId);

        return ResponseEntity.noContent().build();
    }
}