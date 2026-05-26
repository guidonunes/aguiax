package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.VotoIdeiaDto
import br.com.fiap.aguiaexpress.data.dto.toDomain
import br.com.fiap.aguiaexpress.data.remote.api.VotoIdeiaApi
import br.com.fiap.aguiaexpress.domain.model.VotoIdeia

class VotoIdeiaRepository(
    private val votoIdeiaApi: VotoIdeiaApi
) {

    suspend fun votarIdeia(
        ideiaId: Long
    ): Result<VotoIdeia> {
        return try {
            val voto = votoIdeiaApi.votarIdeia(
                ideiaId = ideiaId
            ).toDomain()

            Result.success(voto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removerVotoIdeia(
        ideiaId: Long
    ): Result<Unit> {
        return try {
            votoIdeiaApi.removerVoto(
                ideiaId = ideiaId
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarVotosPorIdeia(
        ideiaId: Long
    ): Result<List<VotoIdeia>> {
        return try {
            val votos = votoIdeiaApi.listarVotosPorIdeia(ideiaId)
                .map { votoDto ->
                    votoDto.toDomain()
                }

            Result.success(votos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarVotosPorUsuario(
        usuarioId: Long
    ): Result<List<VotoIdeia>> {
        return try {
            val votos = votoIdeiaApi.listarVotosPorUsuario(usuarioId)
                .map { votoDto ->
                    votoDto.toDomain()
                }

            Result.success(votos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}