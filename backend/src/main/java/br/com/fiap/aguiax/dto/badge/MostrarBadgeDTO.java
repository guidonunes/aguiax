package br.com.fiap.aguiax.dto.badge;

import br.com.fiap.aguiax.model.Badge;

public record MostrarBadgeDTO(
        Long id,
        String nomeBadge,
        String descricao,
        String criterio,
        String icone,
        Integer status
) {
    public MostrarBadgeDTO(Badge badge) {
        this(
                badge.getId(),
                badge.getNomeBadge(),
                badge.getDescricao(),
                badge.getCriterio(),
                badge.getIcone(),
                badge.getStatus()
        );
    }
}