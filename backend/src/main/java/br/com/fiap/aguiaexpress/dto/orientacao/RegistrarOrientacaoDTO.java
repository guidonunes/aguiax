package br.com.fiap.aguiaexpress.dto.orientacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarOrientacaoDTO(

        @NotBlank(message = "O título da orientação é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição da orientação é obrigatória.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @Size(max = 100, message = "A área estratégica deve ter no máximo 100 caracteres.")
        String areaEstrategica

) {
}