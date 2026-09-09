package br.com.fiap.aguiaexpress.dto;

import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusProjetoDTO(

        @NotNull(message = "O novo status do projeto é obrigatório.")
        NomeStatusProjeto status

) {
}