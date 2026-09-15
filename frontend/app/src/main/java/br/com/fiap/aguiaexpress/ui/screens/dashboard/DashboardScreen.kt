package br.com.fiap.aguiaexpress.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.viewmodel.DashboardViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import br.com.fiap.aguiaexpress.ui.components.AguiaOutlinedButton as OutlinedButton
import androidx.compose.ui.Alignment
import br.com.fiap.aguiaexpress.ui.theme.semanticColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Resumo dos resultados dos projetos e indicadores estratégicos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        DashboardOrientationFilter(
            orientacoes = uiState.orientacoes,
            selectedOrientationId = uiState.orientacaoSelecionadaId,
            enabled = !uiState.isLoading,
            onOrientationSelected = viewModel::selecionarOrientacao
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando dashboard...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar dashboard",
                    onRetry = {
                        viewModel.carregarDashboard()
                    }
                )
            }

            summary == null -> {
                ErrorMessage(
                    message = "O resumo do dashboard não está disponível.",
                    onRetry = viewModel::carregarDashboard
                )
            }

            summary.totalProjects == 0L -> {
                EmptyState(
                    message = if (uiState.orientacaoSelecionadaId == null) {
                        "Nenhum projeto encontrado para gerar o dashboard."
                    } else {
                        "Nenhum projeto encontrado para esta orientação."
                    }
                )
            }

            else -> {
                DashboardMetricsSection(
                    totalProjetos = summary.totalProjects,
                    projetosEmAndamento = summary.projectsInProgress,
                    roiMedio = summary.averageRoi,
                    ganhoProdutividadeMedio = summary.averageProductivityGain
                )

                Spacer(modifier = Modifier.height(24.dp))

                ResumoFinanceiroSection(
                    investimentoTotal = summary.totalInvestment,
                    retornoFinanceiroTotal = summary.totalFinancialReturn,
                    reducaoCustosTotal = summary.totalCostReduction,
                    resultadoEstimado = summary.estimatedResult
                )
                Spacer(modifier = Modifier.height(24.dp))

                ProjetosPorStatusSection(
                    projetosPorStatus = summary.projectsByStatus
                )
            }
        }
    }
}

@Composable
private fun DashboardOrientationFilter(
    orientacoes: List<Orientacao>,
    selectedOrientationId: Long?,
    enabled: Boolean,
    onOrientationSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOrientation = orientacoes.firstOrNull { orientation ->
        orientation.id == selectedOrientationId
    }

    Column {
        Text(
            text = "Orientação estratégica",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedOrientation?.titulo ?: "Todas as orientações"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas as orientações") },
                    onClick = {
                        expanded = false
                        onOrientationSelected(null)
                    }
                )

                orientacoes.forEach { orientation ->
                    DropdownMenuItem(
                        text = { Text(orientation.titulo) },
                        onClick = {
                            expanded = false
                            onOrientationSelected(orientation.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricsSection(
    totalProjetos: Long,
    projetosEmAndamento: Long,
    roiMedio: BigDecimal,
    ganhoProdutividadeMedio: BigDecimal
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "Total de projetos",
                value = totalProjetos.toString(),
                modifier = Modifier.weight(1f)
            )

            DashboardMetricCard(
                title = "Projetos em andamento",
                value = projetosEmAndamento.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "ROI médio",
                value = formatarPercentual(roiMedio),
                modifier = Modifier.weight(1f)
            )

            DashboardMetricCard(
                title = "Produtividade",
                value = formatarPercentual(ganhoProdutividadeMedio),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = valueColor
            )
        }
    }
}

private fun formatarMoeda(valor: BigDecimal): String {
    return java.text.NumberFormat
        .getCurrencyInstance(java.util.Locale.forLanguageTag("pt-BR"))
        .format(valor)
}

private fun formatarPercentual(valor: BigDecimal): String {
    val formatter = java.text.NumberFormat.getNumberInstance(
        java.util.Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(valor)}%"
}

@Composable
private fun ProjetosPorStatusSection(
    projetosPorStatus: Map<String, Long>
) {
    Column {
        Text(
            text = "Projetos por status",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusCountCard(
                label = "Planejados",
                count = projetosPorStatus["PLANEJADO"] ?: 0
            )

            StatusCountCard(
                label = "Em andamento",
                count = projetosPorStatus["EM_ANDAMENTO"] ?: 0
            )

            StatusCountCard(
                label = "Concluídos",
                count = projetosPorStatus["CONCLUIDO"] ?: 0
            )

            StatusCountCard(
                label = "Cancelados",
                count = projetosPorStatus["CANCELADO"] ?: 0
            )
        }
    }
}

@Composable
private fun StatusCountCard(
    label: String,
    count: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ResumoFinanceiroSection(
    investimentoTotal: BigDecimal,
    retornoFinanceiroTotal: BigDecimal,
    reducaoCustosTotal: BigDecimal,
    resultadoEstimado: BigDecimal
) {
    Column {
        Text(
            text = "Resumo financeiro",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                FinancialInfoRow(
                    label = "Investimento total",
                    value = formatarMoeda(investimentoTotal)
                )

                FinancialInfoRow(
                    label = "Retorno total",
                    value = formatarMoeda(retornoFinanceiroTotal)
                )

                FinancialInfoRow(
                    label = "Redução de custos",
                    value = formatarMoeda(reducaoCustosTotal)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FinancialInfoRow(
                    label = "Resultado estimado",
                    value = formatarMoeda(resultadoEstimado),
                    isHighlighted = true
                )
            }
        }
    }
}

@Composable
private fun FinancialInfoRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHighlighted) {
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = if (isHighlighted) {
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            } else {
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            },
            color = if (isHighlighted) {
                MaterialTheme.semanticColors.success
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
