package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.Ideia

data class IdeiaDto(
    val id: Long? = null,
    val usuarioId: Long,
    val categoriaId: Long,
    val orientacaoId: Long,
    val titulo: String,
    val descricao: String,
    val problema: String,
    val solucaoProposta: String,
    val status: String? = null,
    val nomeUsuario: String? = null,
    val nomeCategoria: String? = null,
    val tituloOrientacao: String? = null,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)

fun IdeiaDto.toDomain(): Ideia {
    return Ideia(
        id = id ?: 0L,
        titulo = titulo,
        descricao = descricao,
        problema = problema,
        solucaoProposta = solucaoProposta,
        status = status ?: "NOVA",
        usuarioId = usuarioId,
        categoriaId = categoriaId,
        orientacaoId = orientacaoId,
        nomeUsuario = nomeUsuario ?: "Usuário não informado",
        nomeCategoria = nomeCategoria ?: "Categoria não informada",
        tituloOrientacao = tituloOrientacao ?: "Orientação não informada"
    )
}