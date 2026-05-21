package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.CategoriaDto
import retrofit2.http.GET


interface CategoriaApi {
    @GET("categorias")
    suspend fun listarCategories(): List<CategoriaDto>

}