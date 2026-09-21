package br.com.fiap.aguiax.data.repository

import br.com.fiap.aguiax.data.dto.toDomain
import br.com.fiap.aguiax.data.remote.api.UsuarioBadgeApi
import br.com.fiap.aguiax.domain.model.UsuarioBadge

class UsuarioBadgeRepository(
    private val usuarioBadgeApi: UsuarioBadgeApi
) {

    suspend fun listarBadgesPorUsuario(
        usuarioId: Long
    ): Result<List<UsuarioBadge>> {
        return try {
            val usuarioBadges = usuarioBadgeApi.listarBadgesPorUsuario(usuarioId)
                .map { usuarioBadgeDto ->
                    usuarioBadgeDto.toDomain()
                }

            Result.success(usuarioBadges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}