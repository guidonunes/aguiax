package br.com.fiap.aguiax.data.dto

data class EditarOrientacaoRequestDto(
    val usuarioId: Long,
    val titulo: String,
    val descricao: String,
    val areaEstrategica: String
)