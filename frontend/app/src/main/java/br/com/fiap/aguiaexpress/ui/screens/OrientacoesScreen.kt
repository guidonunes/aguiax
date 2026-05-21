package br.com.fiap.aguiaexpress.ui.screens.orientacoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.OrientacaoViewModel

@Composable
fun OrientacoesScreen(
    viewModel: OrientacaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Orientações Estratégicas",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando orientações...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar orientações",
                    onRetry = {
                        viewModel.carregarOrientacoesAtivas()
                    }
                )
            }

            uiState.orientacoes.isEmpty() -> {
                EmptyState(message = "Nenhuma orientação encontrada.")
            }

            else -> {
                OrientacaoList(
                    orientacoes = uiState.orientacoes
                )
            }
        }
    }
}

@Composable
private fun OrientacaoList(
    orientacoes: List<Orientacao>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orientacoes) { orientacao ->
            OrientacaoCard(orientacao = orientacao)
        }
    }
}

@Composable
private fun OrientacaoCard(
    orientacao: Orientacao
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = orientacao.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = orientacao.descricao,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Área: ${orientacao.areaEstrategica}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Criado por: ${orientacao.nomeUsuario} (${orientacao.perfilUsuario})",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrientacoesScreenPreview() {
    AguiaExpressTheme {
        OrientacoesScreen()
    }

}