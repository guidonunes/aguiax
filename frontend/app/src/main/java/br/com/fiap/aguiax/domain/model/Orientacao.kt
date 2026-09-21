package br.com.fiap.aguiax.domain.model

data class Orientacao(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val areaEstrategica: String,
    val nomeUsuario: String,
    val perfilUsuario: String,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)
