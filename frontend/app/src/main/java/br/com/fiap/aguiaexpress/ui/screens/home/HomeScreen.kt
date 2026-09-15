package br.com.fiap.aguiaexpress.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.HomeUiState
import br.com.fiap.aguiaexpress.viewmodel.HomeViewModel
import java.time.Instant

@Composable
internal fun HomeScreen(onNavigate: (HomeDestination) -> Unit) {
    val model: HomeViewModel = viewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.refresh() }
    HomeContent(state, onNavigate, model::refresh)
}

@Composable
internal fun HomeContent(state: HomeUiState, onNavigate: (HomeDestination) -> Unit, onRefresh: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item(key = "header") { HomeHeader(state.greeting, state.overview?.role, state.isLoading, onRefresh) }
        when {
            state.isLoading -> item { LoadingIndicator(message = "Carregando seu resumo...") }
            state.errorMessage != null -> item { ErrorMessage(message = state.errorMessage, onRetry = onRefresh) }
            state.overview != null -> {
                val overview = state.overview
                if (overview.warnings.isNotEmpty()) item(key = "warnings") { HomeAvailabilityNotice(overview.warnings, onRefresh) }
                item(key = "role-content") {
                    when (overview.role) {
                        ApplicationRole.OPERACIONAL -> OperationalHomeContent(overview, onNavigate)
                        ApplicationRole.GESTOR -> ManagerHomeContent(overview, onNavigate)
                        ApplicationRole.LIDER -> LeaderHomeContent(overview, onNavigate)
                    }
                }
                item(key = "activity") {
                    HomeSection("Atualizações recentes") {
                        if (overview.activities.isEmpty()) EmptyState(message = "Nenhuma atualização com data disponível por enquanto.")
                        else HomeActivityList(overview.activities, onNavigate)
                    }
                }
            }
        }
    }
}

@Composable
internal fun OperationalHomeContent(overview: HomeOverview, onNavigate: (HomeDestination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        HomeMetricGrid(overview.metrics)
        HomeQuickAction("Enviar nova ideia", "Transforme uma oportunidade em inovação.", HomeSymbol.IDEA,
            onClick = { onNavigate(HomeDestination.CREATE_IDEA) }, prominent = true)
        HomeSection("Suas ideias em evolução", "Ver ideias", { onNavigate(HomeDestination.IDEAS) }) {
            if (overview.pipeline.isEmpty()) EmptyState(message = "O andamento das suas ideias não está disponível.")
            else if (overview.pipeline.sumOf { it.count } == 0)
                EmptyState(message = "Sua próxima contribuição começa com uma ideia. Envie a primeira!")
            else HomeStatusSummary(overview.pipeline)
        }
    }
}

@Composable
internal fun ManagerHomeContent(overview: HomeOverview, onNavigate: (HomeDestination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        HomeMetricGrid(overview.metrics)
        HomeSection("Precisa de atenção", "Ver ideias", { onNavigate(HomeDestination.IDEAS) }) {
            if (overview.attention.isEmpty()) EmptyState(message = if (overview.warnings.isEmpty())
                "Nenhuma ideia aguardando revisão ou decisão." else "A lista de pendências pode estar incompleta. Atualize para tentar novamente.")
            else HomeAttentionList(overview.attention, onNavigate)
        }
        HomeSection("Próximos passos") {
            HomeQuickAction("Revisar ideias", "Avalie impacto, viabilidade e prioridade.", HomeSymbol.REVIEW,
                { onNavigate(HomeDestination.IDEAS) })
            HomeQuickAction("Acompanhar projetos", "Veja o progresso e atualize os resultados.", HomeSymbol.PROJECT,
                { onNavigate(HomeDestination.PROJECTS) })
        }
    }
}

@Composable
internal fun LeaderHomeContent(overview: HomeOverview, onNavigate: (HomeDestination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        HomeMetricGrid(overview.metrics.take(4))
        HomeExecutiveHighlights(overview.metrics.drop(4))
        HomeQuickAction("Abrir dashboard completo", "Explore resultados, indicadores e filtros estratégicos.", HomeSymbol.ROI,
            { onNavigate(HomeDestination.DASHBOARD) }, prominent = true)
        HomeSection("Panorama estratégico", "Ver orientações", { onNavigate(HomeDestination.ORIENTATIONS) }) {
            Text("Até 3 orientações, em ordem alfabética. Contagens calculadas pelo backend.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (overview.strategies.isEmpty()) EmptyState(message = if (overview.warnings.isEmpty())
                "Nenhuma orientação estratégica cadastrada." else "O panorama estratégico não está disponível.")
            else HomeStrategyList(overview.strategies)
        }
    }
}

// Sample values exist only in previews; production content uses the authenticated APIs.
private fun previewState(role: ApplicationRole): HomeUiState {
    val metrics = when (role) {
        ApplicationRole.OPERACIONAL -> listOf(
            HomeMetric("Ideias criadas", "8", HomeSymbol.IDEA), HomeMetric("Medalhas conquistadas", "3", HomeSymbol.BADGE),
            HomeMetric("Votos recebidos", "24", HomeSymbol.VOTE), HomeMetric("Ideias aprovadas", "2", HomeSymbol.APPROVED))
        ApplicationRole.GESTOR -> listOf(
            HomeMetric("Aguardando revisão", "6", HomeSymbol.REVIEW), HomeMetric("Ideias priorizadas", "3", HomeSymbol.VOTE),
            HomeMetric("Ideias aprovadas", "2", HomeSymbol.APPROVED), HomeMetric("Projetos ativos", "5", HomeSymbol.PROJECT))
        ApplicationRole.LIDER -> listOf(
            HomeMetric("Projetos em andamento", "4", HomeSymbol.PROJECT), HomeMetric("Investimento total", "R$ 120.000,00", HomeSymbol.INVESTMENT),
            HomeMetric("Retorno financeiro", "R$ 180.000,00", HomeSymbol.RETURN), HomeMetric("ROI médio", "50%", HomeSymbol.ROI),
            HomeMetric("Resultado estimado", "R$ 75.000,00", HomeSymbol.RETURN), HomeMetric("Produtividade média", "12%", HomeSymbol.ROI))
    }
    return HomeUiState("Bom dia, Guilherme", HomeOverview(role, metrics,
        pipeline = listOf(HomeStatusCount("NOVA", 3), HomeStatusCount("EM_ANALISE", 2), HomeStatusCount("PRIORIZADA", 1), HomeStatusCount("APROVADA", 2)),
        attention = listOf(HomeAttention("Otimização de rotas", "Priorizada · pronta para decisão", HomeDestination.IDEAS)),
        strategies = listOf(HomeStrategy(1, "Eficiência operacional", 4), HomeStrategy(2, "Sustentabilidade", 2)),
        activities = listOf(HomeActivity("sample", "Ideia · Em análise", "Otimização de rotas",
            Instant.parse("2026-09-15T12:00:00Z"), HomeSymbol.IDEA, HomeDestination.IDEAS))
    ), isLoading = false)
}

@Preview(showBackground = true, name = "Operacional", widthDp = 390, heightDp = 1000)
@Composable
private fun OperationalPreview() { AguiaExpressTheme { HomeContent(previewState(ApplicationRole.OPERACIONAL), {}, {}) } }

@Preview(showBackground = true, name = "Gestor", widthDp = 390, heightDp = 1000)
@Composable
private fun ManagerPreview() { AguiaExpressTheme { HomeContent(previewState(ApplicationRole.GESTOR), {}, {}) } }

@Preview(showBackground = true, name = "Líder", widthDp = 390, heightDp = 1000)
@Composable
private fun LeaderPreview() { AguiaExpressTheme { HomeContent(previewState(ApplicationRole.LIDER), {}, {}) } }

@Preview(showBackground = true, name = "Operacional escuro", widthDp = 390, heightDp = 1000)
@Composable
private fun DarkPreview() { AguiaExpressTheme(darkTheme = true) { HomeContent(previewState(ApplicationRole.OPERACIONAL), {}, {}) } }

@Preview(showBackground = true, name = "Texto ampliado", widthDp = 320, heightDp = 1200, fontScale = 1.6f)
@Composable
private fun LargeTextPreview() { AguiaExpressTheme { HomeContent(previewState(ApplicationRole.OPERACIONAL), {}, {}) } }
