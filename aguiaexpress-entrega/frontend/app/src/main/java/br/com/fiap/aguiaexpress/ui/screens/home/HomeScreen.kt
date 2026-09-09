package br.com.fiap.aguiaexpress.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onCategoriasClick: () -> Unit,
    onOrientacoesClick: () -> Unit,
    onIdeiasClick: () -> Unit,
    onProjetosClick: () -> Unit,
    onMedalhasClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Águia Express",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Gestão de idéias, projetos e conquistas",
            style = MaterialTheme.typography.bodyLarge
        )
        Button (
            onClick = onCategoriasClick,
            modifier = Modifier.fillMaxWidth()

        ) {
            Text(text = "Categorias")
        }

        Button (
            onClick = onOrientacoesClick,
            modifier = Modifier.fillMaxWidth()

        ) {
            Text(text = "Orientações Estratégicas")
        }

        Button (
            onClick = onIdeiasClick,
            modifier = Modifier.fillMaxWidth()

        ) {
            Text(text = "Idéias")
        }

        Button (
            onClick = onProjetosClick,
            modifier = Modifier.fillMaxWidth()

        ) {
            Text(text = "Projetos")
        }

        Button (
            onClick = onMedalhasClick,
            modifier = Modifier.fillMaxWidth()

        ) {
            Text(text = "Medalhas")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        onCategoriasClick = {},
        onOrientacoesClick = {},
        onIdeiasClick = {},
        onProjetosClick = {},
        onMedalhasClick = {}
    )
}

