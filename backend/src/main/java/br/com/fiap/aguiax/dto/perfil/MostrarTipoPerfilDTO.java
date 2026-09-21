package br.com.fiap.aguiax.dto.perfil;

import br.com.fiap.aguiax.model.NomePerfil;
import br.com.fiap.aguiax.model.TipoPerfil;

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