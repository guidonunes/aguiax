package br.com.fiap.aguiaexpress.dto.projeto;

import br.com.fiap.aguiaexpress.model.NomeStatusProjeto;
import br.com.fiap.aguiaexpress.model.Projeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MostrarProjetoDTO(

        Long id,

        Long ideiaId,
        String tituloIdeia,

        Long usuarioId,
        String nomeUsuarioResponsavel,

        Long statusProjetoId,
        NomeStatusProjeto statusProjeto,

        String titulo,
        String descricao,
        String objetivo,

        BigDecimal investimento,
        BigDecimal roi,
        BigDecimal retornoFinanceiro,
        BigDecimal reducaoCustos,
        BigDecimal ganhoProdutividade,

        LocalDate dataInicio,
        LocalDate dataPrevisaoFim,
        LocalDate dataConclusao,

        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao

) {
    public MostrarProjetoDTO(Projeto projeto) {
        this(
                projeto.getId(),

                projeto.getIdeia().getId(),
                projeto.getIdeia().getTitulo(),

                projeto.getUsuario().getId(),
                projeto.getUsuario().getNome(),

                projeto.getStatusProjeto().getId(),
                projeto.getStatusProjeto().getStatus(),

                projeto.getTitulo(),
                projeto.getDescricao(),
                projeto.getObjetivo(),

                projeto.getInvestimento(),
                projeto.getRoi(),
                projeto.getRetornoFinanceiro(),
                projeto.getReducaoCustos(),
                projeto.getGanhoProdutividade(),

                projeto.getDataInicio(),
                projeto.getDataPrevisaoFim(),
                projeto.getDataConclusao(),

                projeto.getDataCriacao(),
                projeto.getDataAtualizacao()
        );
    }
}