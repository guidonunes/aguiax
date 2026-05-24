package br.com.fiap.aguiaexpress.ui.screens.projetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Projeto
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.components.StatusTag
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.ProjetoViewModel

@Composable
fun ProjetosScreen(
    viewModel: ProjetoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Projetos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Acompanhe projetos gerados a partir das ideias aprovadas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando projetos...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar projetos",
                    onRetry = {
                        viewModel.carregarProjetos()
                    }
                )
            }

            uiState.projetos.isEmpty() -> {
                EmptyState(message = "Nenhum projeto encontrado.")
            }

            else -> {
                ProjetoList(projetos = uiState.projetos)
            }
        }
    }
}

@Composable
private fun ProjetoList(
    projetos: List<Projeto>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(projetos) { projeto ->
            ProjetoCard(projeto = projeto)
        }
    }
}

@Composable
private fun ProjetoCard(
    projeto: Projeto
) {
    var detalhesExpanded by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = projeto.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatusTag(text = formatarStatusProjeto(projeto.statusProjeto))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = projeto.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        detalhesExpanded = !detalhesExpanded
                    }
                ) {
                    Text(
                        text = if (detalhesExpanded) {
                            "Ocultar detalhes"
                        } else {
                            "Ver detalhes"
                        }
                    )

                    Icon(
                        imageVector = if (detalhesExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null
                    )
                }
            }

            if (detalhesExpanded) {
                InfoText(
                    label = "Objetivo",
                    value = projeto.objetivo
                )

                InfoText(
                    label = "Ideia vinculada",
                    value = projeto.tituloIdeia
                )

                InfoText(
                    label = "Responsável",
                    value = projeto.nomeUsuarioResponsavel
                )

                InfoText(
                    label = "Investimento",
                    value = "R$ ${projeto.investimento}"
                )

                InfoText(
                    label = "ROI",
                    value = "${projeto.roi}%"
                )

                InfoText(
                    label = "Retorno financeiro",
                    value = "R$ ${projeto.retornoFinanceiro}"
                )

                InfoText(
                    label = "Redução de custos",
                    value = "R$ ${projeto.reducaoCustos}"
                )

                InfoText(
                    label = "Ganho de produtividade",
                    value = "${projeto.ganhoProdutividade}%"
                )

                InfoText(
                    label = "Data de início",
                    value = projeto.dataInicio
                )

                InfoText(
                    label = "Previsão de fim",
                    value = projeto.dataPrevisaoFim
                )

                if (projeto.dataConclusao != null) {
                    InfoText(
                        label = "Data de conclusão",
                        value = projeto.dataConclusao
                    )
                }
            }
        }
    }
}

private fun formatarStatusProjeto(status: String): String {
    return when (status) {
        "PLANEJADO" -> "Planejado"
        "EM_ANDAMENTO" -> "Em andamento"
        "CONCLUIDO" -> "Concluído"
        "CANCELADO" -> "Cancelado"
        else -> status
    }
}

@Composable
private fun InfoText(
    label: String,
    value: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold)
            ) {
                append("$label: ")
            }

            append(value)
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ProjetosScreenPreview() {
    AguiaExpressTheme() {
        ProjetosScreen()
    }
}