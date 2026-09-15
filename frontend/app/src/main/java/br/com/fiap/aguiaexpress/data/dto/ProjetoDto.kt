package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.Projeto

data class ProjetoDto(
    val id: Long,
    val ideiaId: Long,
    val tituloIdeia: String,
    val usuarioId: Long,
    val nomeUsuarioResponsavel: String,
    val statusProjetoId: Long,
    val statusProjeto: String,
    val titulo: String,
    val descricao: String,
    val objetivo: String,
    val investimento: Double,
    val roi: Double,
    val retornoFinanceiro: Double,
    val reducaoCustos: Double,
    val ganhoProdutividade: Double,
    val dataInicio: String,
    val dataPrevisaoFim: String,
    val dataConclusao: String?,
    val dataCriacao: String,
    val dataAtualizacao: String
)

fun ProjetoDto.toDomain(): Projeto {
    return Projeto(
        id = id,
        ideiaId = ideiaId,
        tituloIdeia = tituloIdeia,
        usuarioId = usuarioId,
        nomeUsuarioResponsavel = nomeUsuarioResponsavel,
        statusProjetoId = statusProjetoId,
        statusProjeto = statusProjeto,
        titulo = titulo,
        descricao = descricao,
        objetivo = objetivo,
        investimento = investimento,
        roi = roi,
        retornoFinanceiro = retornoFinanceiro,
        reducaoCustos = reducaoCustos,
        ganhoProdutividade = ganhoProdutividade,
        dataInicio = dataInicio,
        dataPrevisaoFim = dataPrevisaoFim,
        dataConclusao = dataConclusao,
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao
    )
}
