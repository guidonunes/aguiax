package br.com.fiap.aguiax.dto.perfil;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais usadas para obter um JWT")
public record LoginDTO(

        @NotBlank(message = "Email não pode ser em branco.")
        @Email(message = "Email inválido.")
        @Schema(example = "gestor@aguiax.com")
        String email,

        @NotBlank(message = "Senha não pode ser em branco.")
        @Schema(example = "senha-local", format = "password")
        String senha

) {
}
