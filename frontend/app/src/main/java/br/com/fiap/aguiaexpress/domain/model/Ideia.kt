package br.com.fiap.aguiaexpress.domain.model

data class Ideia(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val problema: String,
    val solucaoProposta: String,
    val status: String,
    val usuarioId: Long,
    val categoriaId: Long,
    val orientacaoId: Long,
    val nomeUsuario: String,
    val nomeCategoria: String,
    val tituloOrientacao: String
)