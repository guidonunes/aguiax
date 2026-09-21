package br.com.fiap.aguiax.data.dto

data class CriarIdeiaRequestDto(
    val usuarioId: Long,
    val categoriaId: Long,
    val orientacaoId: Long,
    val titulo: String,
    val descricao: String,
    val problema: String,
    val solucaoProposta: String
)