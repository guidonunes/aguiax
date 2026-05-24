package br.com.fiap.aguiaexpress.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(

        @NotBlank(message = "Email não pode ser em branco.")
        @Email(message = "Email inválido.")
        String email,

        @NotBlank(message = "Senha não pode ser em branco.")
        String senha

) {
}