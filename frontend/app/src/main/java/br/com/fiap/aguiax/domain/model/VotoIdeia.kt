package br.com.fiap.aguiax.domain.model

data class VotoIdeia(
    val id: Long,
    val usuarioId: Long,
    val nomeUsuario: String,
    val emailUsuario: String,
    val ideiaId: Long,
    val tituloIdeia: String,
    val valor: Int,
    val dataVoto: String
)