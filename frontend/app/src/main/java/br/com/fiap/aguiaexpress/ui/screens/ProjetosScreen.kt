package br.com.fiap.aguiaexpress.ui.screens.projetos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ProjetosScreen() {
    Text(
        text = "Tela de Projetos",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(24.dp)
    )
}

@Preview (showBackground = true)
@Composable
private fun ProjetosScreenPreview() {
    ProjetosScreen()
}