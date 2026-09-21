package br.com.fiap.aguiax.data.dto

import br.com.fiap.aguiax.domain.model.Categoria

data class CategoriaDto(
    val id: Long,
    val nomeCategoria: String,
    val descricao: String?
)

fun CategoriaDto.toDomain(): Categoria {
    return Categoria(
        id = id,
        nome = nomeCategoria,
        descricao = descricao ?: "Sem descrição"
    )
}
