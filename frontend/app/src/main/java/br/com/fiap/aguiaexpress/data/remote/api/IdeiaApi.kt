package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.IdeiaDto
import retrofit2.http.GET
import retrofit2.http.Path

interface IdeiaApi {

    @GET("ideias")
    suspend fun listarIdeias(): List<IdeiaDto>

    @GET("ideias/{id}")
    suspend fun buscarIdeiaPorId(
        @Path("id") id: Long
    ): IdeiaDto
}