package br.com.fiap.aguiaexpress.domain.model

import java.time.Instant

enum class DashboardInsightScope {
    GLOBAL,
    ORIENTATION
}

data class DashboardInsight(
    val scope: DashboardInsightScope,
    val orientationId: Long?,
    val metricsSnapshot: DashboardSummary,
    val executiveSummary: String,
    val highlights: List<String>,
    val attentionPoints: List<String>,
    val recommendations: List<String>,
    val generatedAt: Instant
)
