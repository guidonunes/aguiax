package br.com.fiap.aguiax.dto.ideia;

import br.com.fiap.aguiax.model.NomeStatusIdeia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo status solicitado para a ideia")
public record AtualizarStatusIdeiaDTO(

        @NotNull(message = "O novo status da ideia é obrigatório.")
        @Schema(example = "EM_ANALISE")
        NomeStatusIdeia status

) {
}
