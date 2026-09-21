package br.com.fiap.aguiax.dto.usuario;

import br.com.fiap.aguiax.model.NomePerfil;
import br.com.fiap.aguiax.model.Usuario;

import java.time.LocalDateTime;

public record MostrarUsuarioDTO(
        Long id,
        String nome,
        String email,
        String cargo,
        String departamento,
        Integer pontos,
        Integer status,
        NomePerfil perfil,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
    public MostrarUsuarioDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo(),
                usuario.getDepartamento(),
                usuario.getPontos(),
                usuario.getStatus(),
                usuario.getTipoPerfil().getNomePerfil(),
                usuario.getDataCriacao(),
                usuario.getDataAtualizacao()
        );
    }
}