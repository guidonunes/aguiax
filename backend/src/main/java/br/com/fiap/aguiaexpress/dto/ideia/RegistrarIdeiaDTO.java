package br.com.fiap.aguiaexpress.dto.ideia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarIdeiaDTO(

        @NotNull(message = "O ID da categoria é obrigatório.")
        Long categoriaId,

        @NotNull(message = "O ID da orientação estratégica é obrigatório.")
        Long orientacaoId,

        @NotBlank(message = "O título da ideia é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição da ideia é obrigatória.")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @Size(max = 1000, message = "O problema deve ter no máximo 1000 caracteres.")
        String problema,

        @Size(max = 1000, message = "A solução proposta deve ter no máximo 1000 caracteres.")
        String solucaoProposta

) {
}