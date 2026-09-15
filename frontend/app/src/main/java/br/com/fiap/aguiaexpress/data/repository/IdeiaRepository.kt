package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.AtualizarAvaliacaoIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.AtualizarStatusIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.dto.toDomain
import br.com.fiap.aguiaexpress.data.remote.api.IdeiaApi
import br.com.fiap.aguiaexpress.domain.model.AiIdeaEvaluation
import br.com.fiap.aguiaexpress.domain.model.Ideia
import kotlinx.coroutines.CancellationException

class IdeiaRepository(
    private val ideiaApi: IdeiaApi
) {

    suspend fun listarIdeiasPorUsuario(usuarioId: Long): Result<List<Ideia>> {
        return try {
            Result.success(ideiaApi.listarIdeiasPorUsuario(usuarioId).map { it.toDomain() })
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun listarIdeias(): Result<List<Ideia>> {
        return try {
            val ideias = ideiaApi.listarIdeias()
                .map { ideiaDto ->
                    ideiaDto.toDomain()
                }

            Result.success(ideias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarIdeiaPorId(id: Long): Result<Ideia> {
        return try {
            val ideia = ideiaApi.buscarIdeiaPorId(id).toDomain()

            Result.success(ideia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarIdeia(request: CriarIdeiaRequestDto): Result<Ideia> {
        return try {
            val ideiaCriada = ideiaApi.criarIdeia(request).toDomain()

            Result.success(ideiaCriada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarStatus(
        ideiaId: Long,
        status: String
    ): Result<Ideia> {
        return try {
            val request = AtualizarStatusIdeiaRequestDto(
                status = status
            )

            val ideiaAtualizada = ideiaApi.atualizarStatusIdeia(
                id = ideiaId,
                request = request
            ).toDomain()

            Result.success(ideiaAtualizada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarIdeia(
        ideiaId: Long
    ): Result<Unit> {
        return try {
            ideiaApi.deletarIdeia(id = ideiaId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarIdeia(
        ideiaId: Long,
        request: CriarIdeiaRequestDto
    ): Result<Ideia> {
        return try {
            val ideiaAtualizada = ideiaApi.editarIdeia(
                id = ideiaId,
                request = request
            ).toDomain()

            Result.success(ideiaAtualizada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarAvaliacao(
        ideiaId: Long,
        impacto: Int,
        viabilidade: Int
    ): Result<Ideia> {
        return try {
            val request = AtualizarAvaliacaoIdeiaRequestDto(
                impacto = impacto,
                viabilidade = viabilidade
            )
            val ideiaAtualizada = ideiaApi.atualizarAvaliacaoIdeia(
                id = ideiaId,
                request = request
            ).toDomain()
            Result.success(ideiaAtualizada)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun avaliarIdeiaComIa(
        ideiaId: Long
    ): Result<AiIdeaEvaluation> {
        return try {
            val evaluation = ideiaApi.avaliarIdeiaComIa(
                id = ideiaId
            ).toDomain()

            require(evaluation.ideaId == ideiaId) {
                "A avaliação de IA retornou uma ideia diferente da solicitada."
            }

            Result.success(evaluation)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
