package br.com.fiap.aguiaexpress.dto.projeto;

import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.StatusProjeto;

public record MostrarStatusProjetoDTO(
        Long id,
        NomeStatusProjeto status,
        String descricao
) {
    public MostrarStatusProjetoDTO(StatusProjeto statusProjeto) {
        this(
                statusProjeto.getId(),
                statusProjeto.getStatus(),
                statusProjeto.getDescricao()
        );
    }
}