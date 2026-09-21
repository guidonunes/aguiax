package br.com.fiap.aguiax.data.repository

import br.com.fiap.aguiax.data.dto.toDomain
import br.com.fiap.aguiax.data.remote.api.OrientacaoApi
import br.com.fiap.aguiax.domain.model.Orientacao
import br.com.fiap.aguiax.data.dto.CriarOrientacaoRequestDto
import br.com.fiap.aguiax.data.dto.EditarOrientacaoRequestDto
import br.com.fiap.aguiax.data.dto.toDomain


class OrientacaoRepository(
    private val orientacaoApi: OrientacaoApi
) {
    suspend fun listarOrientacoes(): Result<List<Orientacao>> {
        return try {
            val orientacoes = orientacaoApi.listarOrientacoes()
                .map { orientacaoDto ->
                    orientacaoDto.toDomain()
                }
            Result.success(orientacoes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarOrientacoesAtivas(): Result<List<Orientacao>> {
        return try {
            val orientacoes = orientacaoApi.listarOrientacoesAtivas()
                .map { orientacaoDto ->
                    orientacaoDto.toDomain()
                }
            Result.success(orientacoes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criarOrientacao(
        request: CriarOrientacaoRequestDto
    ): Result<Orientacao> {
        return try {
            val orientacaoCriada = orientacaoApi.criarOrientacao(request).toDomain()

            Result.success(orientacaoCriada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarOrientacaoPorId(
        orientacaoId: Long
    ): Result<Orientacao> {
        return try {
            val orientacao = orientacaoApi.buscarOrientacaoPorId(orientacaoId).toDomain()

            Result.success(orientacao)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarOrientacao(
        orientacaoId: Long,
        request: EditarOrientacaoRequestDto
    ): Result<Orientacao> {
        return try {
            val orientacaoAtualizada = orientacaoApi.editarOrientacao(
                id = orientacaoId,
                request = request
            ).toDomain()

            Result.success(orientacaoAtualizada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarOrientacao(
        orientacaoId: Long,
        usuarioId: Long
    ): Result<Unit> {
        return try {
            orientacaoApi.deletarOrientacao(
                id = orientacaoId,
                usuarioId = usuarioId
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
