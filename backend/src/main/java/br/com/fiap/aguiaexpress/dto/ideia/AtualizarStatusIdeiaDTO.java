package br.com.fiap.aguiaexpress.dto.ideia;

import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusIdeiaDTO(

        @NotNull(message = "O novo status da ideia é obrigatório.")
        NomeStatusIdeia status

) {
}