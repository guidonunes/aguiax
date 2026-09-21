package br.com.fiap.aguiax.data.dto

import br.com.fiap.aguiax.domain.model.Badge

data class BadgeDto(
    val id: Long,
    val nomeBadge: String,
    val descricao: String,
    val criterio: String,
    val icone: String,
    val status: Int
)

fun BadgeDto.toDomain(): Badge {
    return Badge(
        id = id,
        nome = nomeBadge,
        descricao = descricao,
        criterio = criterio,
        icone = icone,
        status = status
    )
}