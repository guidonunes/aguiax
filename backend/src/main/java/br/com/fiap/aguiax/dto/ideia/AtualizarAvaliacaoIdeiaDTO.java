package br.com.fiap.aguiax.dto.ideia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Pontuações atribuídas pelo gestor, ambas na escala de 0 a 10")
public record AtualizarAvaliacaoIdeiaDTO(

        @NotNull(message = "O impacto é obrigatório.")
        @Min(value = 0, message = "O impacto deve estar entre 0 e 10.")
        @Max(value = 10, message = "O impacto deve estar entre 0 e 10.")
        @Schema(minimum = "0", maximum = "10", example = "8")
        Integer impacto,

        @NotNull(message = "A viabilidade é obrigatória.")
        @Min(value = 0, message = "A viabilidade deve estar entre 0 e 10.")
        @Max(value = 10, message = "A viabilidade deve estar entre 0 e 10.")
        @Schema(minimum = "0", maximum = "10", example = "7")
        Integer viabilidade

) {
}
