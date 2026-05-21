package br.com.fiap.aguiaexpress.ui.screens.ideias

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IdeiasScreen() {
    Text(
        text = "Tela de Ideias",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(24.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun IdeiasScreenPreview() {
    IdeiasScreen()

}