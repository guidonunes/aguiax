package br.com.fiap.aguiax.data.remote.api

import br.com.fiap.aguiax.data.dto.LoginRequestDto
import br.com.fiap.aguiax.data.dto.TokenResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): TokenResponseDto
}