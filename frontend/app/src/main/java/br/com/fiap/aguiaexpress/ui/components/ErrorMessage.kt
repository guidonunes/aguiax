package br.com.fiap.aguiaexpress.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import br.com.fiap.aguiaexpress.ui.components.AguiaButton as Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme

@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )

        if (onRetry != null) {
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Tentar novamente")
            }
        }
    }
}

@Preview (showBackground = true)
@Composable
private fun ErrorMessagePreview() {
    AguiaExpressTheme() {
        ErrorMessage(
            message = "Erro ao carregar os dados."
        )

    }

}
