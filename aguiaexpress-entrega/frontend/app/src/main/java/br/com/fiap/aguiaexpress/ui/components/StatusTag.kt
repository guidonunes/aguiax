package br.com.fiap.aguiaexpress.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme

@Composable
fun StatusTag(
    text: String,
    modifier: Modifier = Modifier
) {
    val normalizedText = text.uppercase()

    val backgroundColor = when (normalizedText) {
        "NOVA", "NOVO", "PLANEJADO" -> Color(0xFFE3F2FD)
        "EM ANÁLISE", "EM_ANALISE" -> Color(0xFFE8EAF6)
        "PRIORIZADA" -> Color(0xFFFFF3E0)
        "APROVADA", "CONCLUÍDO", "CONCLUIDO" -> Color(0xFFE8F5E9)
        "REJEITADA", "CANCELADO" -> Color(0xFFFFEBEE)
        "CONVERTIDA EM PROJETO", "CONVERTIDA_EM_PROJETO" -> Color(0xFFF3E5F5)
        "EM ANDAMENTO", "EM_ANDAMENTO" -> Color(0xFFFFF8E1)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (normalizedText) {
        "NOVA", "NOVO", "PLANEJADO" -> Color(0xFF1565C0)
        "EM ANÁLISE", "EM_ANALISE" -> Color(0xFF3949AB)
        "PRIORIZADA" -> Color(0xFFE65100)
        "APROVADA", "CONCLUÍDO", "CONCLUIDO" -> Color(0xFF2E7D32)
        "REJEITADA", "CANCELADO" -> Color(0xFFC62828)
        "CONVERTIDA EM PROJETO", "CONVERTIDA_EM_PROJETO" -> Color(0xFF6A1B9A)
        "EM ANDAMENTO", "EM_ANDAMENTO" -> Color(0xFFF57F17)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Preview (showBackground = true)
@Composable
private fun StatusTagPreview() {
    AguiaExpressTheme() {
        StatusTag(text = "Em andamento")
    }
}