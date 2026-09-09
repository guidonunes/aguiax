package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.core.session.SessionManager
import br.com.fiap.aguiaexpress.data.dto.LoginRequestDto
import br.com.fiap.aguiaexpress.data.dto.TokenResponseDto
import br.com.fiap.aguiaexpress.data.remote.api.AuthApi

class AuthRepository(
    private val authApi: AuthApi
) {

    suspend fun login(
        email: String,
        senha: String
    ): Result<TokenResponseDto> {
        return try {
            val request = LoginRequestDto(
                email = email,
                senha = senha
            )

            val response = authApi.login(request)

            SessionManager.salvarSessao(
                token = response.token,
                usuarioId = response.usuarioId,
                nome = response.nome,
                email = response.email,
                perfil = response.perfil
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        SessionManager.limparSessao()
    }
}