package br.com.fiap.aguiaexpress.ui.components



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.fiap.aguiaexpress.ui.theme.SemanticTone
import br.com.fiap.aguiaexpress.ui.theme.toneColors
import androidx.compose.ui.unit.dp

@Composable
fun PerfilTag(
    perfil: String,
    modifier: Modifier = Modifier
) {
    val normalizedPerfil = perfil.uppercase()

    val colors = toneColors(when (normalizedPerfil) {
        "LIDER", "LÍDER" -> SemanticTone.ACHIEVEMENT
        "GESTOR", "GESTÃO", "GESTAO" -> SemanticTone.INFO
        "OPERACIONAL" -> SemanticTone.SUCCESS
        else -> SemanticTone.NEUTRAL
    })

    Box(
        modifier = modifier
            .background(
                color = colors.container,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = formatarPerfil(perfil),
            style = MaterialTheme.typography.labelSmall,
            color = colors.content
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
