package br.com.fiap.aguiaexpress.dto;

import br.com.fiap.aguiaexpress.model.UsuarioBadge;

import java.time.LocalDateTime;

public record MostrarUsuarioBadgeDTO(

        Long id,

        Long usuarioId,
        String nomeUsuario,
        String emailUsuario,

        Long badgeId,
        String nomeBadge,
        String descricaoBadge,
        String criterioBadge,
        String iconeBadge,

        LocalDateTime dataConquista

) {
    public MostrarUsuarioBadgeDTO(UsuarioBadge usuarioBadge) {
        this(
                usuarioBadge.getId(),

                usuarioBadge.getUsuario().getId(),
                usuarioBadge.getUsuario().getNome(),
                usuarioBadge.getUsuario().getEmail(),

                usuarioBadge.getBadge().getId(),
                usuarioBadge.getBadge().getNomeBadge(),
                usuarioBadge.getBadge().getDescricao(),
                usuarioBadge.getBadge().getCriterio(),
                usuarioBadge.getBadge().getIcone(),

                usuarioBadge.getDataConquista()
        );
    }
}