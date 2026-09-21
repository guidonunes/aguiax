package br.com.fiap.aguiax.data.remote.api

import br.com.fiap.aguiax.data.dto.UsuarioBadgeDto
import retrofit2.http.GET
import retrofit2.http.Path

interface UsuarioBadgeApi {

    @GET("usuario-badges/usuario/{usuarioId}")
    suspend fun listarBadgesPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): List<UsuarioBadgeDto>
}