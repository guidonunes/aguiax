package br.com.fiap.aguiaexpress.dto.perfil;

import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.TipoPerfil;

public record MostrarTipoPerfilDTO(
        Long id,
        NomePerfil nome,
        String descricao
) {
    public MostrarTipoPerfilDTO(TipoPerfil tipoPerfil) {
        this(
                tipoPerfil.getId(),
                tipoPerfil.getNomePerfil(),
                tipoPerfil.getDescricaoPerfil()
        );
    }
}