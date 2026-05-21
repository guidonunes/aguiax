package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.toDomain
import br.com.fiap.aguiaexpress.data.remote.api.CategoriaApi
import br.com.fiap.aguiaexpress.domain.model.Categoria


class CategoriaRepository (
    private val categoriaApi: CategoriaApi
) {
    suspend fun listarCategorias(): Result<List<Categoria>> {
        return try {
            val categorias = categoriaApi.listarCategorias()
                .map { categoriaDto ->
                    categoriaDto.toDomain() }
            Result.success(categorias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}