package br.com.fiap.aguiax.data.remote.api

import br.com.fiap.aguiax.data.dto.AtualizarStatusProjetoRequestDto
import br.com.fiap.aguiax.data.dto.CriarProjetoRequestDto
import br.com.fiap.aguiax.data.dto.EditarProjetoRequestDto
import br.com.fiap.aguiax.data.dto.ProjetoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProjetoApi {

    @GET("projetos")
    suspend fun listarProjetos(): List<ProjetoDto>

    @GET("projetos/{id}")
    suspend fun buscarProjetoPorId(
        @Path("id") id: Long
    ): ProjetoDto

    @POST("projetos")
    suspend fun criarProjeto(
        @Body request: CriarProjetoRequestDto
    ): ProjetoDto

    @PUT("projetos/{id}")
    suspend fun editarProjeto(
        @Path("id") id: Long,
        @Body request: EditarProjetoRequestDto
    ): ProjetoDto

    @DELETE("projetos/{id}")
    suspend fun deletarProjeto(
        @Path("id") id: Long
    )

    @PATCH("projetos/{id}/status")
    suspend fun atualizarStatusProjeto(
        @Path("id") id: Long,
        @Body request: AtualizarStatusProjetoRequestDto
    ): ProjetoDto
}