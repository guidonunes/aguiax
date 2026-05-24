package br.com.fiap.aguiaexpress.ui.components



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PerfilTag(
    perfil: String,
    modifier: Modifier = Modifier
) {
    val normalizedPerfil = perfil.uppercase()

    val backgroundColor = when (normalizedPerfil) {
        "LIDER", "LÍDER" -> Color(0xFFEDE7F6)
        "GESTOR", "GESTÃO", "GESTAO" -> Color(0xFFE3F2FD)
        "OPERACIONAL" -> Color(0xFFE8F5E9)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (normalizedPerfil) {
        "LIDER", "LÍDER" -> Color(0xFF5E35B1)
        "GESTOR", "GESTÃO", "GESTAO" -> Color(0xFF1565C0)
        "OPERACIONAL" -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = formatarPerfil(perfil),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

private fun formatarPerfil(perfil: String): String {
    return when (perfil.uppercase()) {
        "LIDER", "LÍDER" -> "Líder"
        "GESTOR", "GESTÃO", "GESTAO" -> "Gestor"
        "OPERACIONAL" -> "Operacional"
        else -> perfil
    }
}