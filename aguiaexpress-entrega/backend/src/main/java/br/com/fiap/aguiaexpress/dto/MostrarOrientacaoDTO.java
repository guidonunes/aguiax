package br.com.fiap.aguiaexpress.dto;

import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.OrientacaoEstrategica;

import java.time.LocalDateTime;

public record MostrarOrientacaoDTO(

        Long id,
        String titulo,
        String descricao,
        String areaEstrategica,
        Integer status,

        Long usuarioId,
        String nomeUsuario,
        NomePerfil perfilUsuario,

        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao

) {
    public MostrarOrientacaoDTO(OrientacaoEstrategica orientacao) {
        this(
                orientacao.getId(),
                orientacao.getTitulo(),
                orientacao.getDescricao(),
                orientacao.getAreaEstrategica(),
                orientacao.getStatus(),

                orientacao.getUsuario().getId(),
                orientacao.getUsuario().getNome(),
                orientacao.getUsuario().getTipoPerfil().getNomePerfil(),

                orientacao.getDataCriacao(),
                orientacao.getDataAtualizacao()
        );
    }
}