package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.AtualizarStatusIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.IdeiaDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface IdeiaApi {

    @GET("ideias")
    suspend fun listarIdeias(): List<IdeiaDto>

    @GET("ideias/{id}")
    suspend fun buscarIdeiaPorId(
        @Path("id") id: Long
    ): IdeiaDto

    @POST("ideias")
    suspend fun criarIdeia(
        @Body request: CriarIdeiaRequestDto
    ): IdeiaDto

    @PATCH("ideias/{id}/status")
    suspend fun atualizarStatusIdeia(
        @Path("id") id: Long,
        @Body request: AtualizarStatusIdeiaRequestDto
    ): IdeiaDto
}