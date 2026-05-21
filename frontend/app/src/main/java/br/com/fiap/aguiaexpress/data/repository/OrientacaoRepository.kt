package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.toDomain
import br.com.fiap.aguiaexpress.data.remote.api.OrientacaoApi
import br.com.fiap.aguiaexpress.domain.model.Orientacao

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
}
