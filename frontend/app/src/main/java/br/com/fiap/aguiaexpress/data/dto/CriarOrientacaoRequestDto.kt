package br.com.fiap.aguiaexpress.data.dto

data class CriarOrientacaoRequestDto(
    val usuarioId: Long,
    val titulo: String,
    val descricao: String,
    val areaEstrategica: String
)