package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.BadgeDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BadgeApi {

    @GET("badges")
    suspend fun listarBadges(): List<BadgeDto>

    @GET("badges/ativas")
    suspend fun listarBadgesAtivas(): List<BadgeDto>

    @GET("badges/{id}")
    suspend fun buscarBadgePorId(
        @Path("id") id: Long
    ): BadgeDto
}