package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.toDomain
import br.com.fiap.aguiaexpress.data.remote.api.ProjetoApi
import br.com.fiap.aguiaexpress.domain.model.Projeto

class ProjetoRepository(
    private val projetoApi: ProjetoApi
) {

    suspend fun listarProjetos(): Result<List<Projeto>> {
        return try {
            val projetos = projetoApi.listarProjetos()
                .map { projetoDto ->
                    projetoDto.toDomain()
                }

            Result.success(projetos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarProjetoPorId(id: Long): Result<Projeto> {
        return try {
            val projeto = projetoApi.buscarProjetoPorId(id).toDomain()

            Result.success(projeto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}