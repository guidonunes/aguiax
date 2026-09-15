package br.com.fiap.aguiaexpress.ui.screens.home

import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.domain.model.HomeInputs
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.domain.model.Projeto
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal enum class HomeDestination { IDEAS, PROJECTS, BADGES, ORIENTATIONS, DASHBOARD, CREATE_IDEA }
internal enum class HomeSymbol { IDEA, BADGE, VOTE, APPROVED, REVIEW, PROJECT, INVESTMENT, RETURN, ROI, STRATEGY }
internal data class HomeMetric(val label: String, val value: String, val symbol: HomeSymbol, val hint: String? = null)
internal data class HomeStatusCount(val status: String, val count: Int)
internal data class HomeActivity(
    val id: String,
    val description: String,
    val title: String,
    val timestamp: Instant,
    val symbol: HomeSymbol,
    val destination: HomeDestination
)
internal data class HomeAttention(val title: String, val detail: String, val destination: HomeDestination)
internal data class HomeStrategy(val id: Long, val title: String, val projectCount: Long?)
internal data class HomeOverview(
    val role: ApplicationRole,
    val metrics: List<HomeMetric>,
    val pipeline: List<HomeStatusCount> = emptyList(),
    val attention: List<HomeAttention> = emptyList(),
    val strategies: List<HomeStrategy> = emptyList(),
    val activities: List<HomeActivity> = emptyList(),
    val warnings: List<String> = emptyList()
)

internal fun homeGreeting(name: String?, hour: Int): String {
    val greeting = when (hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    val firstName = name?.trim()?.split(Regex("\\s+"))?.firstOrNull()?.takeIf { it.isNotBlank() }
    return if (firstName == null) greeting else "$greeting, $firstName"
}

internal fun homeStatusLabel(status: String): String = when (status) {
    "NOVA" -> "Nova"
    "EM_ANALISE" -> "Em análise"
    "PRIORIZADA" -> "Priorizada"
    "APROVADA" -> "Aprovada"
    "REJEITADA" -> "Rejeitada"
    "CONVERTIDA_EM_PROJETO" -> "Convertida em projeto"
    "PLANEJADO" -> "Planejado"
    "EM_ANDAMENTO" -> "Em andamento"
    "CONCLUIDO" -> "Concluído"
    "CANCELADO" -> "Cancelado"
    else -> status.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }
}

internal fun homeTimestamp(value: String?, zone: ZoneId = ZoneId.systemDefault()): Instant? {
    if (value.isNullOrBlank()) return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).atZone(zone).toInstant() }.getOrNull()
}

internal fun formatHomeTimestamp(value: Instant): String = DateTimeFormatter
    .ofPattern("dd MMM · HH:mm", Locale.forLanguageTag("pt-BR"))
    .withZone(ZoneId.systemDefault()).format(value)

private fun homeMoney(value: BigDecimal?): String? = value?.let {
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(it)
}

private fun homePercentage(value: BigDecimal?): String? = value?.let {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"))
    formatter.maximumFractionDigits = 2
    "${formatter.format(it)}%"
}

private fun metric(label: String, value: Any?, symbol: HomeSymbol, hint: String? = null) =
    HomeMetric(label, value?.toString() ?: "—", symbol, if (value == null) "Indisponível" else hint)

private fun ideaActivity(idea: Ideia): HomeActivity? {
    val timestamp = homeTimestamp(idea.dataAtualizacao) ?: homeTimestamp(idea.dataCriacao) ?: return null
    return HomeActivity(
        "idea-${idea.id}", "Ideia · ${homeStatusLabel(idea.status)}", idea.titulo,
        timestamp, HomeSymbol.IDEA, HomeDestination.IDEAS
    )
}

private fun projectActivity(project: Projeto): HomeActivity? {
    val timestamp = homeTimestamp(project.dataAtualizacao) ?: homeTimestamp(project.dataCriacao) ?: return null
    return HomeActivity(
        "project-${project.id}", "Projeto · ${homeStatusLabel(project.statusProjeto)}", project.titulo,
        timestamp, HomeSymbol.PROJECT, HomeDestination.PROJECTS
    )
}

internal fun buildHomeOverview(role: ApplicationRole, userId: Long, inputs: HomeInputs): HomeOverview {
    val activities = mutableListOf<HomeActivity>()
    val metrics: List<HomeMetric>
    var pipeline = emptyList<HomeStatusCount>()
    var attention = emptyList<HomeAttention>()
    var strategies = emptyList<HomeStrategy>()
    when (role) {
        ApplicationRole.OPERACIONAL -> {
            val ideas = inputs.ideas?.filter { it.usuarioId == userId }?.distinctBy { it.id }
            val badges = inputs.badges?.filter { it.usuarioId == userId }?.distinctBy { it.badgeId }
            val votes = ideas?.takeIf { list -> list.all { it.totalVotos != null && it.totalVotos >= 0 } }
                ?.sumOf { checkNotNull(it.totalVotos).toLong() }
            metrics = listOf(
                metric("Ideias criadas", ideas?.size, HomeSymbol.IDEA),
                metric("Medalhas conquistadas", badges?.size, HomeSymbol.BADGE),
                metric("Votos recebidos", votes, HomeSymbol.VOTE),
                metric("Ideias aprovadas", ideas?.count { it.status in setOf("APROVADA", "CONVERTIDA_EM_PROJETO") },
                    HomeSymbol.APPROVED, "Inclui as convertidas em projeto")
            )
            pipeline = ideas?.let { list ->
                listOf("NOVA", "EM_ANALISE", "PRIORIZADA", "APROVADA", "CONVERTIDA_EM_PROJETO", "REJEITADA")
                    .map { status -> HomeStatusCount(status, list.count { it.status == status }) }
            }.orEmpty()
            activities += ideas.orEmpty().mapNotNull(::ideaActivity)
            activities += badges.orEmpty().mapNotNull { badge ->
                homeTimestamp(badge.dataConquista)?.let { timestamp ->
                    HomeActivity("badge-${badge.id}", "Medalha conquistada", badge.nomeBadge, timestamp,
                        HomeSymbol.BADGE, HomeDestination.BADGES)
                }
            }
            val ownIdeaIds = ideas.orEmpty().map { it.id }.toSet()
            activities += inputs.projects.orEmpty().filter { it.ideiaId in ownIdeaIds }.mapNotNull(::projectActivity)
        }
        ApplicationRole.GESTOR -> {
            val ideas = inputs.ideas
            val projects = inputs.projects
            metrics = listOf(
                metric("Aguardando revisão", ideas?.count { it.status in setOf("NOVA", "EM_ANALISE") }, HomeSymbol.REVIEW),
                metric("Ideias priorizadas", ideas?.count { it.status == "PRIORIZADA" }, HomeSymbol.VOTE),
                metric("Ideias aprovadas", ideas?.count { it.status == "APROVADA" }, HomeSymbol.APPROVED),
                metric("Projetos ativos", projects?.count { it.statusProjeto in setOf("PLANEJADO", "EM_ANDAMENTO") },
                    HomeSymbol.PROJECT, "Planejados e em andamento")
            )
            val candidates = ideas.orEmpty().filter { it.status in setOf("NOVA", "EM_ANALISE", "PRIORIZADA", "APROVADA") }
                .sortedWith(compareBy<Ideia> {
                    when (it.status) { "PRIORIZADA" -> 0; "NOVA", "EM_ANALISE" -> 1; else -> 2 }
                }.thenByDescending { homeTimestamp(it.dataAtualizacao) ?: homeTimestamp(it.dataCriacao) }.thenByDescending { it.id })
            attention = candidates.take(3).map { idea ->
                val detail = when (idea.status) {
                    "PRIORIZADA" -> "Priorizada · pronta para decisão"
                    "APROVADA" -> "Aprovada · pronta para conversão em projeto"
                    else -> if (idea.impacto == null || idea.viabilidade == null) "Avaliação de impacto e viabilidade pendente"
                        else "${homeStatusLabel(idea.status)} · revisão e decisão pendentes"
                }
                HomeAttention(idea.titulo, detail, HomeDestination.IDEAS)
            }
            activities += ideas.orEmpty().mapNotNull(::ideaActivity)
            activities += projects.orEmpty().mapNotNull(::projectActivity)
        }
        ApplicationRole.LIDER -> {
            val summary = inputs.summary
            metrics = listOf(
                metric("Projetos em andamento", summary?.projectsInProgress, HomeSymbol.PROJECT),
                metric("Investimento total", homeMoney(summary?.totalInvestment), HomeSymbol.INVESTMENT),
                metric("Retorno financeiro", homeMoney(summary?.totalFinancialReturn), HomeSymbol.RETURN),
                metric("ROI médio", homePercentage(summary?.averageRoi), HomeSymbol.ROI),
                metric("Resultado estimado", homeMoney(summary?.estimatedResult), HomeSymbol.RETURN),
                metric("Produtividade média", homePercentage(summary?.averageProductivityGain), HomeSymbol.ROI)
            )
            strategies = inputs.orientations.orEmpty().sortedBy { it.titulo }.take(3).map {
                HomeStrategy(it.id, it.titulo, inputs.strategySummaries[it.id]?.totalProjects)
            }
            activities += inputs.orientations.orEmpty().mapNotNull { orientation ->
                val timestamp = homeTimestamp(orientation.dataAtualizacao) ?: homeTimestamp(orientation.dataCriacao)
                timestamp?.let {
                    HomeActivity("orientation-${orientation.id}", "Orientação estratégica", orientation.titulo,
                        it, HomeSymbol.STRATEGY, HomeDestination.ORIENTATIONS)
                }
            }
        }
    }
    return HomeOverview(role, metrics, pipeline, attention, strategies,
        activities.distinctBy { it.id }.sortedWith(compareByDescending<HomeActivity> { it.timestamp }.thenBy { it.id }).take(5),
        inputs.warnings)
}
