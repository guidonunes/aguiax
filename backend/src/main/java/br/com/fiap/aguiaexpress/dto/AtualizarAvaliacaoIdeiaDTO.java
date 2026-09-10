package br.com.fiap.aguiaexpress.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarAvaliacaoIdeiaDTO(

        @NotNull(message = "O impacto é obrigatório.")
        @Min(value = 0, message = "O impacto deve estar entre 0 e 10.")
        @Max(value = 10, message = "O impacto deve estar entre 0 e 10.")
        Integer impacto,

        @NotNull(message = "A viabilidade é obrigatória.")
        @Min(value = 0, message = "A viabilidade deve estar entre 0 e 10.")
        @Max(value = 10, message = "A viabilidade deve estar entre 0 e 10.")
        Integer viabilidade

) {
}
