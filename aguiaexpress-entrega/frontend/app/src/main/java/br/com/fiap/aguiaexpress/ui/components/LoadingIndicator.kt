package br.com.fiap.aguiaexpress.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme

@Composable
fun LoadingIndicator(
    message: String = "Carregando..."
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        CircularProgressIndicator()

        Text(
            text = message,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Preview (showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    AguiaExpressTheme() {
        LoadingIndicator()
    }

}