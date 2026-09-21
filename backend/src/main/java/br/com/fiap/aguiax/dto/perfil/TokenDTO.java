package br.com.fiap.aguiax.dto.perfil;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sessão autenticada retornada pelo login")
public record TokenDTO(
        @Schema(description = "JWT usado como Bearer token")
        String token,
        Long usuarioId,
        String nome,
        String email,
        @Schema(allowableValues = {"OPERACIONAL", "GESTOR", "LIDER"})
        String perfil
) {
}
