package br.com.fiap.aguiax.data.dto

import br.com.fiap.aguiax.domain.model.Orientacao

data class OrientacaoDto(
    val id: Long,
    val titulo: String,
    val descricao: String?,
    val areaEstrategica: String,
    val status: Int,
    val usuarioId: Long,
    val nomeUsuario: String,
    val perfilUsuario: String,
    val dataCriacao: String,
    val dataAtualizacao: String
)


fun OrientacaoDto.toDomain(): Orientacao {
    return Orientacao(
        id = id,
        titulo = titulo,
        descricao = descricao ?: "Sem descrição",
        areaEstrategica = areaEstrategica,
        nomeUsuario = nomeUsuario,
        perfilUsuario = perfilUsuario,
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao
    )
}
