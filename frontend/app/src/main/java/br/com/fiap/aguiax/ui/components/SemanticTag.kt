package br.com.fiap.aguiax.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiax.ui.theme.SemanticTone
import br.com.fiap.aguiax.ui.theme.toneColors

@Composable
internal fun SemanticTag(text: String, tone: SemanticTone, modifier: Modifier = Modifier) {
    val colors = toneColors(tone)
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = colors.container, contentColor = colors.content) {
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}
