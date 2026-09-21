package br.com.fiap.aguiax.data.remote.api

import br.com.fiap.aguiax.data.dto.DashboardInsightDto
import br.com.fiap.aguiax.data.dto.DashboardSummaryDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DashboardApi {

    @GET("api/dashboard/summary")
    suspend fun buscarResumo(
        @Query("orientationId") orientationId: Long? = null
    ): DashboardSummaryDto

    @POST("api/dashboard/insights")
    suspend fun gerarInsight(
        @Query("orientationId") orientationId: Long? = null
    ): DashboardInsightDto
}
