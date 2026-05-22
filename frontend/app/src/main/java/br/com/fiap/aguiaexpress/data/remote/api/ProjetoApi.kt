package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.ProjetoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjetoApi {

    @GET("projetos")
    suspend fun listarProjetos(): List<ProjetoDto>

    @GET("projetos/{id}")
    suspend fun buscarProjetoPorId(
        @Path("id") id: Long
    ): ProjetoDto
}