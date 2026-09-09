package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.VotoIdeiaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VotoIdeiaApi {

    @POST("votos-ideias/ideia/{ideiaId}")
    suspend fun votarIdeia(
        @Path("ideiaId") ideiaId: Long
    ): VotoIdeiaDto

    @DELETE("votos-ideias/ideia/{ideiaId}")
    suspend fun removerVoto(
        @Path("ideiaId") ideiaId: Long
    )

    @GET("votos-ideias/ideia/{ideiaId}")
    suspend fun listarVotosPorIdeia(
        @Path("ideiaId") ideiaId: Long
    ): List<VotoIdeiaDto>

    @GET("votos-ideias/usuario/{usuarioId}")
    suspend fun listarVotosPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): List<VotoIdeiaDto>
}