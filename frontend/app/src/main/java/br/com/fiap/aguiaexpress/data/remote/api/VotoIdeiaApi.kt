package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.VotoIdeiaDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VotoIdeiaApi {

    @POST("votos-ideias/usuario/{usuarioId}/ideia/{ideiaId}")
    suspend fun votarIdeia(
        @Path("usuarioId") usuarioId: Long,
        @Path("ideiaId") ideiaId: Long
    ): VotoIdeiaDto

    @DELETE("votos-ideias/usuario/{usuarioId}/ideia/{ideiaId}")
    suspend fun removerVotoIdeia(
        @Path("usuarioId") usuarioId: Long,
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