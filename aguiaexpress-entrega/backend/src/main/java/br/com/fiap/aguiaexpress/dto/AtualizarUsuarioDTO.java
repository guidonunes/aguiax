package br.com.fiap.aguiaexpress.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AtualizarUsuarioDTO(

        @NotBlank(message = "Nome do usuário não pode ser em branco.")
        String nome,

        @NotBlank(message = "Email não pode ser em branco.")
        @Email(message = "Email inválido.")
        String email,

        String cargo,

        String departamento

) {
}