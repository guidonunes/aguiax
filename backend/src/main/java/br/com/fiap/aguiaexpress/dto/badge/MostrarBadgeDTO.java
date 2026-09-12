package br.com.fiap.aguiaexpress.dto.badge;

import br.com.fiap.aguiaexpress.model.Badge;

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