package br.com.fiap.aguiax.data.dto

data class CriarOrientacaoRequestDto(
    val usuarioId: Long,
    val titulo: String,
    val descricao: String,
    val areaEstrategica: String
)