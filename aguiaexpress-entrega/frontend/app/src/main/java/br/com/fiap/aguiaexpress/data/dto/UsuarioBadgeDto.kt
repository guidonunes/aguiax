package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge

data class UsuarioBadgeDto(
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

fun UsuarioBadgeDto.toDomain(): UsuarioBadge {
    return UsuarioBadge(
        id = id,
        usuarioId = usuarioId,
        nomeUsuario = nomeUsuario,
        emailUsuario = emailUsuario,
        badgeId = badgeId,
        nomeBadge = nomeBadge,
        descricaoBadge = descricaoBadge,
        criterioBadge = criterioBadge,
        iconeBadge = iconeBadge,
        dataConquista = dataConquista
    )
}