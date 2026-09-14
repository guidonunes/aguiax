package br.com.fiap.aguiaexpress.dto.projeto;

import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo status solicitado para o projeto")
public record AtualizarStatusProjetoDTO(

        @NotNull(message = "O novo status do projeto é obrigatório.")
        @Schema(example = "EM_ANDAMENTO")
        NomeStatusProjeto status

) {
}
