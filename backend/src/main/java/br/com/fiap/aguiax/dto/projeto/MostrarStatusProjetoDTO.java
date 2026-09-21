package br.com.fiap.aguiax.dto.projeto;

import br.com.fiap.aguiax.model.NomeStatusProjeto;
import br.com.fiap.aguiax.model.StatusProjeto;

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