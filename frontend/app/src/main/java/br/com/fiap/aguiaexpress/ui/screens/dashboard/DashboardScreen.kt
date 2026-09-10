package br.com.fiap.aguiaexpress.ui.screens.dashboard

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

            uiState.projetos.isEmpty() -> {
                EmptyState(
                    message = "Nenhum projeto encontrado para gerar o dashboard."
                )
            }

            else -> {
                DashboardMetricsSection(
                    projetosEmAndamento = uiState.projetosEmAndamento,
                    investimentoTotal = uiState.investimentoTotal,
                    retornoFinanceiroTotal = uiState.retornoFinanceiroTotal,
                    reducaoCustosTotal = uiState.reducaoCustosTotal,
                    roiMedio = uiState.roiMedio,
                    ganhoProdutividadeMedio = uiState.ganhoProdutividadeMedio
                )

                Spacer(modifier = Modifier.height(24.dp))

                ResumoFinanceiroSection(
                    investimentoTotal = uiState.investimentoTotal,
                    retornoFinanceiroTotal = uiState.retornoFinanceiroTotal,
                    reducaoCustosTotal = uiState.reducaoCustosTotal,
                    resultadoEstimado = uiState.resultadoEstimado
                )
                Spacer(modifier = Modifier.height(24.dp))

                ProjetosPorStatusSection(
                    projetosPorStatus = uiState.projetosPorStatus
                )

                Spacer(modifier = Modifier.height(24.dp))


            }
        }
    }
}

@Composable
private fun DashboardMetricsSection(
    projetosEmAndamento: Int,
    investimentoTotal: Double,
    retornoFinanceiroTotal: Double,
    reducaoCustosTotal: Double,
    roiMedio: Double,
    ganhoProdutividadeMedio: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "Projetos em andamento",
                value = projetosEmAndamento.toString(),
                modifier = Modifier.weight(1f)
            )

            DashboardMetricCard(
                title = "ROI médio",
                value = formatarPercentual(roiMedio),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "Investimento",
                value = formatarMoeda(investimentoTotal),
                modifier = Modifier.weight(1f),
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            DashboardMetricCard(
                title = "Retorno",
                value = formatarMoeda(retornoFinanceiroTotal),
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFF2E7D32)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "Redução custos",
                value = formatarMoeda(reducaoCustosTotal),
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFF2E7D32)
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

private fun formatarMoeda(valor: Double): String {
    return java.text.NumberFormat
        .getCurrencyInstance(java.util.Locale("pt", "BR"))
        .format(valor)
}

private fun formatarPercentual(valor: Double): String {
    return String.format(
        java.util.Locale("pt", "BR"),
        "%.1f%%",
        valor
    )
}

@Composable
private fun ProjetosPorStatusSection(
    projetosPorStatus: Map<String, Int>
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
    count: Int
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
    investimentoTotal: Double,
    retornoFinanceiroTotal: Double,
    reducaoCustosTotal: Double,
    resultadoEstimado: Double
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
                Color(0xFF2E7D32)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
