package br.com.fiap.aguiax.domain.model

import java.math.BigDecimal

data class Ideia(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val problema: String,
    val solucaoProposta: String,
    val impacto: Int?,
    val viabilidade: Int?,
    val prioridade: BigDecimal,
    val status: String,
    val usuarioId: Long,
    val categoriaId: Long,
    val orientacaoId: Long,
    val nomeUsuario: String,
    val nomeCategoria: String,
    val tituloOrientacao: String,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null,
    val totalVotos: Int? = null
)
