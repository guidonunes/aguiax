package br.com.fiap.aguiaexpress.data.dto

data class EditarOrientacaoRequestDto(
    val usuarioId: Long,
    val titulo: String,
    val descricao: String,
    val areaEstrategica: String
)