package br.com.fiap.aguiax.data.repository

import br.com.fiap.aguiax.data.dto.toDomain
import br.com.fiap.aguiax.data.remote.api.BadgeApi
import br.com.fiap.aguiax.domain.model.Badge

class BadgeRepository(
    private val badgeApi: BadgeApi
) {

    suspend fun listarBadges(): Result<List<Badge>> {
        return try {
            val badges = badgeApi.listarBadges()
                .map { badgeDto ->
                    badgeDto.toDomain()
                }

            Result.success(badges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarBadgesAtivas(): Result<List<Badge>> {
        return try {
            val badges = badgeApi.listarBadgesAtivas()
                .map { badgeDto ->
                    badgeDto.toDomain()
                }

            Result.success(badges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarBadgePorId(id: Long): Result<Badge> {
        return try {
            val badge = badgeApi.buscarBadgePorId(id).toDomain()

            Result.success(badge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}