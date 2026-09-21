package br.com.fiap.aguiax.service;

import br.com.fiap.aguiax.dto.badge.MostrarBadgeDTO;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.model.Badge;
import br.com.fiap.aguiax.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public List<MostrarBadgeDTO> listarBadges() {
        return badgeRepository.findAll()
                .stream()
                .map(MostrarBadgeDTO::new)
                .toList();
    }

    public List<MostrarBadgeDTO> listarBadgesAtivas() {
        return badgeRepository.findByStatus(1)
                .stream()
                .map(MostrarBadgeDTO::new)
                .toList();
    }

    public MostrarBadgeDTO buscarBadgePorId(Long id) {
        return new MostrarBadgeDTO(buscarEntidadePorId(id));
    }

    public MostrarBadgeDTO buscarBadgePorNome(String nomeBadge) {
        Badge badge = buscarEntidadePorNome(nomeBadge);

        return new MostrarBadgeDTO(badge);
    }

    public Badge buscarEntidadePorId(Long id) {
        return badgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge não encontrada."));
    }

    public Badge buscarEntidadePorNome(String nomeBadge) {
        return badgeRepository.findByNomeBadge(nomeBadge)
                .orElseThrow(() -> new ResourceNotFoundException("Badge não encontrada."));
    }
}