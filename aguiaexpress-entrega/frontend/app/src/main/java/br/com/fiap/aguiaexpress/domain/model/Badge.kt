package br.com.fiap.aguiaexpress.domain.model

data class Badge(
    val id: Long,
    val nome: String,
    val descricao: String,
    val criterio: String,
    val icone: String,
    val status: Int
)