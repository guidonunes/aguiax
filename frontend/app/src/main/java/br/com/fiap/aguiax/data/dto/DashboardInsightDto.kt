package br.com.fiap.aguiax.data.dto

import br.com.fiap.aguiax.domain.model.DashboardInsight
import br.com.fiap.aguiax.domain.model.DashboardInsightScope
import java.time.DateTimeException
import java.time.Instant

data class DashboardInsightDto(
    val scope: String?,
    val orientationId: Long?,
    val metricsSnapshot: DashboardSummaryDto?,
    val executiveSummary: String?,
    val highlights: List<String?>?,
    val attentionPoints: List<String?>?,
    val recommendations: List<String?>?,
    val generatedAt: String?
)

fun DashboardInsightDto.toDomain(): DashboardInsight {
    val mappedScope = try {
        DashboardInsightScope.valueOf(
            requireNotNull(scope) {
                "A análise executiva não contém o escopo."
            }
        )
    } catch (exception: IllegalArgumentException) {
        throw IllegalArgumentException(
            "A análise executiva contém um escopo inválido.",
            exception
        )
    }

    when (mappedScope) {
        DashboardInsightScope.GLOBAL -> require(orientationId == null) {
            "A análise global não pode conter uma orientação."
        }

        DashboardInsightScope.ORIENTATION -> require(
            orientationId != null && orientationId > 0
        ) {
            "A análise por orientação requer um identificador positivo."
        }
    }

    val mappedMetrics = requireNotNull(metricsSnapshot) {
        "A análise executiva não contém o retrato das métricas."
    }.toDomain()
    require(mappedMetrics.totalProjects > 0) {
        "A análise executiva requer ao menos um projeto."
    }

    val mappedGeneratedAt = try {
        Instant.parse(
            requireNotNull(generatedAt) {
                "A análise executiva não contém a data de geração."
            }
        )
    } catch (exception: DateTimeException) {
        throw IllegalArgumentException(
            "A análise executiva contém uma data de geração inválida.",
            exception
        )
    }

    return DashboardInsight(
        scope = mappedScope,
        orientationId = orientationId,
        metricsSnapshot = mappedMetrics,
        executiveSummary = requireText(
            executiveSummary,
            "resumo executivo",
            MAXIMUM_SUMMARY_LENGTH
        ),
        highlights = requireItems(highlights, "destaques", 1, 3),
        attentionPoints = requireItems(
            attentionPoints,
            "pontos de atenção",
            0,
            3
        ),
        recommendations = requireItems(
            recommendations,
            "sugestões de melhoria",
            1,
            3
        ),
        generatedAt = mappedGeneratedAt
    )
}

private fun requireItems(
    values: List<String?>?,
    fieldName: String,
    minimumSize: Int,
    maximumSize: Int
): List<String> {
    val receivedValues = requireNotNull(values) {
        "A análise executiva não contém $fieldName."
    }
    require(receivedValues.size in minimumSize..maximumSize) {
        "A lista de $fieldName deve conter entre $minimumSize e $maximumSize itens."
    }

    return receivedValues.map { value ->
        requireText(value, fieldName, MAXIMUM_ITEM_LENGTH)
    }
}

private fun requireText(
    value: String?,
    fieldName: String,
    maximumLength: Int
): String {
    val mappedValue = value?.trim()
    require(!mappedValue.isNullOrEmpty()) {
        "A análise executiva não contém $fieldName."
    }
    require(mappedValue.length <= maximumLength) {
        "O campo $fieldName excede o tamanho permitido."
    }
    return mappedValue
}

private const val MAXIMUM_SUMMARY_LENGTH = 800
private const val MAXIMUM_ITEM_LENGTH = 300
