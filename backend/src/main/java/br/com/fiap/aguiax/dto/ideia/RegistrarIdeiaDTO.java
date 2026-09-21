package br.com.fiap.aguiax.dto.ideia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou editar uma ideia")
public record RegistrarIdeiaDTO(

        @NotNull(message = "O ID da categoria é obrigatório.")
        @Schema(example = "1")
        Long categoriaId,

        @NotNull(message = "O ID da orientação estratégica é obrigatório.")
        @Schema(example = "1")
        Long orientacaoId,

        @NotBlank(message = "O título da ideia é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        @Schema(example = "Reduzir consumo de combustível")
        String titulo,

        @NotBlank(message = "A descrição da ideia é obrigatória.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        @Schema(example = "Otimização baseada em dados de rota")
        String descricao,

        @Size(max = 1000, message = "O problema deve ter no máximo 1000 caracteres.")
        @Schema(example = "Rotas ineficientes aumentam o consumo")
        String problema,

        @Size(max = 1000, message = "A solução proposta deve ter no máximo 1000 caracteres.")
        @Schema(example = "Recomendar rotas com menor consumo previsto")
        String solucaoProposta

) {
}
