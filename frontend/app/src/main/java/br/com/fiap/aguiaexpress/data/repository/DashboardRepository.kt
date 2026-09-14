package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.toDomain
import br.com.fiap.aguiaexpress.data.remote.api.DashboardApi
import br.com.fiap.aguiaexpress.domain.model.DashboardSummary

class DashboardRepository(
    private val dashboardApi: DashboardApi
) {

    suspend fun buscarResumo(
        orientationId: Long? = null
    ): Result<DashboardSummary> {
        return try {
            Result.success(
                dashboardApi.buscarResumo(orientationId).toDomain()
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
