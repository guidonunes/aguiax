package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.AtualizarAvaliacaoIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.AtualizarStatusIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.AiIdeaEvaluationDto
import br.com.fiap.aguiaexpress.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.IdeiaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @DELETE("ideias/{id}")
    suspend fun deletarIdeia(
        @Path("id") id: Long
    )

    @PUT("ideias/{id}")
    suspend fun editarIdeia(
        @Path("id") id: Long,
        @Body request: CriarIdeiaRequestDto
    ): IdeiaDto

    @PATCH("ideias/{id}/avaliacao")
    suspend fun atualizarAvaliacaoIdeia(
        @Path("id") id: Long,
        @Body request: AtualizarAvaliacaoIdeiaRequestDto
    ): IdeiaDto

    @POST("api/ideas/{id}/ai-evaluation")
    suspend fun avaliarIdeiaComIa(
        @Path("id") id: Long
    ): AiIdeaEvaluationDto
}
