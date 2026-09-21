package br.com.fiap.aguiax.domain.model

data class UsuarioBadge(
    val id: Long,
    val usuarioId: Long,
    val nomeUsuario: String,
    val emailUsuario: String,
    val badgeId: Long,
    val nomeBadge: String,
    val descricaoBadge: String,
    val criterioBadge: String,
    val iconeBadge: String,
    val dataConquista: String
)