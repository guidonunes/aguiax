package br.com.fiap.aguiaexpress.dto.perfil;

public record TokenDTO(
        String token,
        Long usuarioId,
        String nome,
        String email,
        String perfil
) {
}