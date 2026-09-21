package br.com.fiap.aguiax.data.dto

import br.com.fiap.aguiax.domain.model.AiIdeaEvaluation
import java.math.BigDecimal

data class AiIdeaEvaluationDto(
    val ideaId: Long?,
    val strategicAlignment: Int?,
    val impact: Int?,
    val feasibility: Int?,
    val innovation: Int?,
    val overallScore: BigDecimal?,
    val summary: String?
)

fun AiIdeaEvaluationDto.toDomain(): AiIdeaEvaluation {
    val mappedIdeaId = requireNotNull(ideaId) {
        "A avaliação de IA não contém o identificador da ideia."
    }
    require(mappedIdeaId > 0) {
        "O identificador da ideia na avaliação de IA deve ser positivo."
    }

    val mappedOverallScore = requireNotNull(overallScore) {
        "A avaliação de IA não contém a pontuação geral."
    }
    require(mappedOverallScore >= BigDecimal.ZERO && mappedOverallScore <= BigDecimal.TEN) {
        "A pontuação geral da avaliação de IA deve estar entre 0 e 10."
    }

    val mappedSummary = summary?.trim()
    require(!mappedSummary.isNullOrEmpty()) {
        "A avaliação de IA não contém um resumo."
    }

    return AiIdeaEvaluation(
        ideaId = mappedIdeaId,
        strategicAlignment = requireScore(
            strategicAlignment,
            "alinhamento estratégico"
        ),
        impact = requireScore(impact, "impacto"),
        feasibility = requireScore(feasibility, "viabilidade"),
        innovation = requireScore(innovation, "inovação"),
        overallScore = mappedOverallScore,
        summary = mappedSummary
    )
}

private fun requireScore(score: Int?, fieldName: String): Int {
    val mappedScore = requireNotNull(score) {
        "A avaliação de IA não contém a pontuação de $fieldName."
    }
    require(mappedScore in 0..10) {
        "A pontuação de $fieldName deve estar entre 0 e 10."
    }
    return mappedScore
}
