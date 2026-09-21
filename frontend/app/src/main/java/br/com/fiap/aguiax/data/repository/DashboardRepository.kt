package br.com.fiap.aguiax.data.repository

import br.com.fiap.aguiax.data.dto.toDomain
import br.com.fiap.aguiax.data.remote.api.DashboardApi
import br.com.fiap.aguiax.domain.model.DashboardInsight
import br.com.fiap.aguiax.domain.model.DashboardInsightScope
import br.com.fiap.aguiax.domain.model.DashboardSummary

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

    suspend fun gerarInsight(
        orientationId: Long? = null
    ): Result<DashboardInsight> {
        return try {
            val insight = dashboardApi.gerarInsight(orientationId).toDomain()

            if (orientationId == null) {
                require(
                    insight.scope == DashboardInsightScope.GLOBAL &&
                        insight.orientationId == null
                ) {
                    "A análise global retornou um escopo diferente do solicitado."
                }
            } else {
                require(
                    insight.scope == DashboardInsightScope.ORIENTATION &&
                        insight.orientationId == orientationId
                ) {
                    "A análise retornou uma orientação diferente da solicitada."
                }
            }

            Result.success(insight)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
