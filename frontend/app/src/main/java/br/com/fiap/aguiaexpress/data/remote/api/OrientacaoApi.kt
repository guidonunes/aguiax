package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.OrientacaoDto
import retrofit2.http.GET

interface OrientacaoApi {

    @GET("orientacoes")
    suspend fun listarOrientacoes(): List<OrientacaoDto>

    @GET("orientacoes/ativas")
    suspend fun listarOrientacoesAtivas(): List<OrientacaoDto>
}