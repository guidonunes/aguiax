package br.com.fiap.aguiax.domain.model

internal data class HomeInputs(
    // null means unavailable; an empty list is a successfully loaded empty result.
    val ideas: List<Ideia>? = null,
    val badges: List<UsuarioBadge>? = null,
    val projects: List<Projeto>? = null,
    val orientations: List<Orientacao>? = null,
    val summary: DashboardSummary? = null,
    val strategySummaries: Map<Long, DashboardSummary> = emptyMap(),
    val warnings: List<String> = emptyList()
)
