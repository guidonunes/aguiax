package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.VotoIdeia

data class VotoIdeiaDto(
    val id: Long,
    val usuarioId: Long,
    val nomeUsuario: String,
    val emailUsuario: String,
    val ideiaId: Long,
    val tituloIdeia: String,
    val valor: Int,
    val dataVoto: String
)

fun VotoIdeiaDto.toDomain(): VotoIdeia {
    return VotoIdeia(
        id = id,
        usuarioId = usuarioId,
        nomeUsuario = nomeUsuario,
        emailUsuario = emailUsuario,
        ideiaId = ideiaId,
        tituloIdeia = tituloIdeia,
        valor = valor,
        dataVoto = dataVoto
    )
}