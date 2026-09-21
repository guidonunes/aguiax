package br.com.fiap.aguiax.ui.screens.categorias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import br.com.fiap.aguiax.domain.model.Categoria
import br.com.fiap.aguiax.ui.components.EmptyState
import br.com.fiap.aguiax.ui.components.ErrorMessage
import br.com.fiap.aguiax.ui.components.LoadingIndicator
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import br.com.fiap.aguiax.viewmodel.CategoriaViewModel
import androidx.compose.foundation.lazy.items

@Composable
fun CategoriasScreen(
    viewModel: CategoriaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Categorias",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar categorias",
                    onRetry = {
                        viewModel.carregarCategorias()
                    }
                )
            }

            uiState.categorias.isEmpty() -> {
                EmptyState(message = "Nenhuma categoria encontrada")
            }

            else -> {
                CategoriaList(categorias = uiState.categorias)
            }
        }
    }
}

@Composable
fun CategoriaList(
    categorias: List<Categoria>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categorias) {
            categoria -> CategoriaCard(categoria = categoria)
        }
    }
}

@Composable
fun CategoriaCard(
    categoria: Categoria
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)

        ) {
            Text(
                text = categoria.nome,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = categoria.descricao,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview (showBackground = true)
@Composable
private fun CategoriasScreenPreview() {
    AguiaXTheme {
        CategoriasScreen()
    }

}