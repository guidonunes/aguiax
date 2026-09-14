package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.Ideia
import java.math.BigDecimal

data class IdeiaDto(
    val id: Long? = null,
    val usuarioId: Long,
    val categoriaId: Long,
    val orientacaoId: Long,
    val titulo: String,
    val descricao: String,
    val problema: String,
    val solucaoProposta: String,
    val impacto: Int?,
    val viabilidade: Int?,
    val prioridade: BigDecimal,
    val statusIdeia: String? = null,
    val nomeUsuario: String? = null,
    val nomeCategoria: String? = null,
    val tituloOrientacao: String? = null,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)

fun IdeiaDto.toDomain(): Ideia {
    val mappedStatus = requireNotNull(statusIdeia) {
        "A resposta da ideia não contém o campo obrigatório statusIdeia."
    }

    return Ideia(
        id = id ?: 0L,
        titulo = titulo,
        descricao = descricao,
        problema = problema,
        solucaoProposta = solucaoProposta,
        impacto = impacto,
        viabilidade = viabilidade,
        prioridade = prioridade,
        status = mappedStatus,
        usuarioId = usuarioId,
        categoriaId = categoriaId,
        orientacaoId = orientacaoId,
        nomeUsuario = nomeUsuario ?: "Usuário não informado",
        nomeCategoria = nomeCategoria ?: "Categoria não informada",
        tituloOrientacao = tituloOrientacao ?: "Orientação não informada"
    )
}
