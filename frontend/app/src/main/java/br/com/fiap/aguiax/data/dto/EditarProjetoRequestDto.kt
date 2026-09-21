package br.com.fiap.aguiax.data.dto

data class EditarProjetoRequestDto(
    val ideiaId: Long,
    val usuarioId: Long,
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
    val dataConclusao: String?
)