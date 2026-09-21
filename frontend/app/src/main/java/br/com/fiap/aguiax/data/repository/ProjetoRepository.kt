package br.com.fiap.aguiax.data.repository

import br.com.fiap.aguiax.data.dto.AtualizarStatusProjetoRequestDto
import br.com.fiap.aguiax.data.dto.CriarProjetoRequestDto
import br.com.fiap.aguiax.data.dto.EditarProjetoRequestDto
import br.com.fiap.aguiax.data.dto.toDomain
import br.com.fiap.aguiax.data.remote.api.ProjetoApi
import br.com.fiap.aguiax.domain.model.Projeto

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

    suspend fun criarProjeto(
        request: CriarProjetoRequestDto
    ): Result<Projeto> {
        return try {
            val projetoCriado = projetoApi.criarProjeto(request).toDomain()

            Result.success(projetoCriado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarProjeto(
        projetoId: Long,
        request: EditarProjetoRequestDto
    ): Result<Projeto> {
        return try {
            val projetoAtualizado = projetoApi.editarProjeto(
                id = projetoId,
                request = request
            ).toDomain()

            Result.success(projetoAtualizado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarProjeto(
        projetoId: Long
    ): Result<Unit> {
        return try {
            projetoApi.deletarProjeto(id = projetoId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarStatusProjeto(
        projetoId: Long,
        status: String
    ): Result<Projeto> {
        return try {
            val request = AtualizarStatusProjetoRequestDto(
                status = status
            )

            val projetoAtualizado = projetoApi.atualizarStatusProjeto(
                id = projetoId,
                request = request
            ).toDomain()

            Result.success(projetoAtualizado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}