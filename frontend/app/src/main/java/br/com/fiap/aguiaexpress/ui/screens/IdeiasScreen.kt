package br.com.fiap.aguiaexpress.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.components.StatusTag
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.IdeiaViewModel

@Composable
fun IdeiasScreen(
    onCriarIdeiaClick: () -> Unit,
    viewModel: IdeiaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Ideias",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCriarIdeiaClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cadastrar nova ideia")
        }

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando ideias...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar ideias",
                    onRetry = {
                        viewModel.carregarIdeias()
                    }
                )
            }

            uiState.ideias.isEmpty() -> {
                EmptyState(message = "Nenhuma ideia encontrada.")
            }

            else -> {
                IdeiaList(
                    ideias = uiState.ideias
                )
            }
        }
    }
}

@Composable
private fun IdeiaList(
    ideias: List<Ideia>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ideias) { ideia ->
            IdeiaCard(ideia = ideia)
        }
    }
}

@Composable
private fun IdeiaCard(
    ideia: Ideia
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = ideia.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = ideia.descricao,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Problema: ${ideia.problema}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Solução proposta: ${ideia.solucaoProposta}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Categoria: ${ideia.nomeCategoria}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Orientação: ${ideia.tituloOrientacao}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Autor: ${ideia.nomeUsuario}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Column(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                StatusTag(text = ideia.status)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IdeiasScreenPreview() {
    AguiaExpressTheme {
        IdeiasScreen(
            onCriarIdeiaClick = {}
        )
    }
}