package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.CriarOrientacaoRequestDto
import br.com.fiap.aguiaexpress.data.dto.EditarOrientacaoRequestDto
import br.com.fiap.aguiaexpress.data.dto.OrientacaoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrientacaoApi {

    @GET("orientacoes")
    suspend fun listarOrientacoes(): List<OrientacaoDto>

    @GET("orientacoes/ativas")
    suspend fun listarOrientacoesAtivas(): List<OrientacaoDto>

    @POST("orientacoes")
    suspend fun criarOrientacao(
        @Body request: CriarOrientacaoRequestDto
    ): OrientacaoDto

    @PUT("orientacoes/{id}")
    suspend fun editarOrientacao(
        @Path("id") id: Long,
        @Body request: EditarOrientacaoRequestDto
    ): OrientacaoDto

    @GET("orientacoes/{id}")
    suspend fun buscarOrientacaoPorId(
        @Path("id") id: Long
    ): OrientacaoDto

    @DELETE("orientacoes/{id}")
    suspend fun deletarOrientacao(
        @Path("id") id: Long,
        @Query("usuarioId") usuarioId: Long
    )
}