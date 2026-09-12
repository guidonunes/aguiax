package br.com.fiap.aguiaexpress.dto.usuario;

import br.com.fiap.aguiaexpress.model.NomePerfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarUsuarioDTO(

        @NotBlank(message = "Nome do usuário não pode ser em branco.")
        String nome,

        @NotBlank(message = "Email não pode ser em branco.")
        @Email(message = "Email inválido.")
        String email,

        @NotBlank(message = "Senha não pode ser em branco.")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres.")
        String senha,

        @NotNull(message = "Perfil do usuário é obrigatório.")
        NomePerfil nomePerfil,

        String cargo,

        String departamento
) {
}