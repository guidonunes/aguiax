package br.com.fiap.aguiaexpress.data.remote.api

import br.com.fiap.aguiaexpress.data.dto.DashboardSummaryDto
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {

    @GET("api/dashboard/summary")
    suspend fun buscarResumo(
        @Query("orientationId") orientationId: Long? = null
    ): DashboardSummaryDto
}
