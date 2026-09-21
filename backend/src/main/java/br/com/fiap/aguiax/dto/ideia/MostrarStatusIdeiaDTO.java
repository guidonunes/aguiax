package br.com.fiap.aguiax.dto.ideia;

import br.com.fiap.aguiax.model.NomeStatusIdeia;
import br.com.fiap.aguiax.model.StatusIdeia;

public record MostrarStatusIdeiaDTO(
        Long id,
        NomeStatusIdeia status,
        String descricao
) {
    public MostrarStatusIdeiaDTO(StatusIdeia statusIdeia) {
        this(
                statusIdeia.getId(),
                statusIdeia.getStatus(),
                statusIdeia.getDescricao()
        );
    }
}