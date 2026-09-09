package br.com.fiap.aguiaexpress.domain.model

enum class StatusIdeia(
    val label: String
) {
    NOVA("Nova"),
    EM_ANALISE("Em análise"),
    PRIORIZADA("Priorizada"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada"),
    CONVERTIDA_EM_PROJETO("Convertida em projeto")
}