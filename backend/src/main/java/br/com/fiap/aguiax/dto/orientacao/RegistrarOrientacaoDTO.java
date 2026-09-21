package br.com.fiap.aguiax.dto.orientacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou editar uma orientação estratégica")
public record RegistrarOrientacaoDTO(

        @NotBlank(message = "O título da orientação é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        @Schema(example = "Eficiência operacional")
        String titulo,

        @NotBlank(message = "A descrição da orientação é obrigatória.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        @Schema(example = "Reduzir desperdícios e melhorar a produtividade")
        String descricao,

        @Size(max = 100, message = "A área estratégica deve ter no máximo 100 caracteres.")
        @Schema(example = "Operações")
        String areaEstrategica

) {
}
