package br.com.fiap.aguiaexpress.dto.ideia;

import br.com.fiap.aguiaexpress.model.Ideia;
import br.com.fiap.aguiaexpress.model.NomeStatusIdeia;

import java.time.LocalDateTime;

public record MostrarIdeiaDTO(

        Long id,

        String titulo,
        String descricao,
        String problema,
        String solucaoProposta,

        Integer impacto,
        Integer viabilidade,
        Integer prioridade,
        Integer totalVotos,

        Long usuarioId,
        String nomeUsuario,

        Long categoriaId,
        String nomeCategoria,

        Long statusIdeiaId,
        NomeStatusIdeia statusIdeia,

        Long orientacaoId,
        String tituloOrientacao,
        String areaEstrategica,

        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao

) {
    public MostrarIdeiaDTO(Ideia ideia) {
        this(
                ideia.getId(),

                ideia.getTitulo(),
                ideia.getDescricao(),
                ideia.getProblema(),
                ideia.getSolucaoProposta(),

                ideia.getImpacto(),
                ideia.getViabilidade(),
                ideia.getPrioridade(),
                ideia.getTotalVotos(),

                ideia.getUsuario().getId(),
                ideia.getUsuario().getNome(),

                ideia.getCategoria().getId(),
                ideia.getCategoria().getNomeCategoria(),

                ideia.getStatusIdeia().getId(),
                ideia.getStatusIdeia().getStatus(),

                ideia.getOrientacaoEstrategica().getId(),
                ideia.getOrientacaoEstrategica().getTitulo(),
                ideia.getOrientacaoEstrategica().getAreaEstrategica(),

                ideia.getDataCriacao(),
                ideia.getDataAtualizacao()
        );
    }
}