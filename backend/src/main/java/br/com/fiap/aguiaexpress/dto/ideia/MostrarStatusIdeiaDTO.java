package br.com.fiap.aguiaexpress.dto.ideia;

import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import br.com.fiap.aguiaexpress.model.StatusIdeia;

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