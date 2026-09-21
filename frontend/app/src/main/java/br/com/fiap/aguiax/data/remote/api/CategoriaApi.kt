package br.com.fiap.aguiax.data.remote.api

import br.com.fiap.aguiax.data.dto.CategoriaDto
import retrofit2.http.GET


interface CategoriaApi {
    @GET("categorias")
    suspend fun listarCategorias(): List<CategoriaDto>

}