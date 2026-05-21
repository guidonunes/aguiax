package br.com.fiap.aguiaexpress.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme

@Composable
fun EmptyState(
    message: String = "Nenhum item encontrado."
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(24.dp)
    )
}

@Preview (showBackground = true)
@Composable
private fun EmptyStatePreview() {
    AguiaExpressTheme() {
        EmptyState()
    }

}