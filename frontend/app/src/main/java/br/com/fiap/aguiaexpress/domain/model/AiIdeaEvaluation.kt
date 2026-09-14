package br.com.fiap.aguiaexpress.domain.model

import java.math.BigDecimal

data class AiIdeaEvaluation(
    val ideaId: Long,
    val strategicAlignment: Int,
    val impact: Int,
    val feasibility: Int,
    val innovation: Int,
    val overallScore: BigDecimal,
    val summary: String
)
