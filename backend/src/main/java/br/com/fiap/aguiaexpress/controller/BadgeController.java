package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.dto.MostrarBadgeDTO;
import br.com.fiap.aguiaexpress.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<List<MostrarBadgeDTO>> listarBadges() {
        List<MostrarBadgeDTO> badges = badgeService.listarBadges();

        return ResponseEntity.ok(badges);
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<MostrarBadgeDTO>> listarBadgesAtivas() {
        List<MostrarBadgeDTO> badges = badgeService.listarBadgesAtivas();

        return ResponseEntity.ok(badges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MostrarBadgeDTO> buscarBadgePorId(
            @PathVariable Long id
    ) {
        MostrarBadgeDTO badge = badgeService.buscarBadgePorId(id);

        return ResponseEntity.ok(badge);
    }

    @GetMapping("/nome/{nomeBadge}")
    public ResponseEntity<MostrarBadgeDTO> buscarBadgePorNome(
            @PathVariable String nomeBadge
    ) {
        MostrarBadgeDTO badge = badgeService.buscarBadgePorNome(nomeBadge);

        return ResponseEntity.ok(badge);
    }
}