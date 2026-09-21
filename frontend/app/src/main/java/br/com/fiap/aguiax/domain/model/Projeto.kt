package br.com.fiap.aguiax.domain.model

data class Projeto(
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
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)
