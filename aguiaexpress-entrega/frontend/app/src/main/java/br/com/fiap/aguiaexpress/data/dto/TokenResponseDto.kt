package br.com.fiap.aguiaexpress.data.dto

data class TokenResponseDto(
    val token: String,
    val usuarioId: Long,
    val nome: String,
    val email: String,
    val perfil: String
)