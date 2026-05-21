package br.com.fiap.aguiaexpress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarCategoriaDTO(

        @NotBlank(message = "Nome da categoria não pode ser em branco.")
        @Size(max = 80, message = "Nome da categoria deve ter no máximo 80 caracteres.")
        String nomeCategoria,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres.")
        String descricao

) {
}