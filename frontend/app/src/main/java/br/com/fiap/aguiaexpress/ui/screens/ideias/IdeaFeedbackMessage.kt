package br.com.fiap.aguiaexpress.ui.screens.ideias

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import br.com.fiap.aguiaexpress.ui.theme.semanticColors
import br.com.fiap.aguiaexpress.ui.theme.semanticColorsFor
import androidx.compose.ui.unit.dp

internal fun ideaSuccessColors(darkTheme: Boolean): Pair<Color, Color> = semanticColorsFor(darkTheme).let {
    it.successContainer to it.onSuccessContainer
}

@Composable
internal fun FeedbackMessage(message: String, isError: Boolean) {
    val (backgroundColor, textColor) = if (isError) {
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.semanticColors.successContainer to MaterialTheme.semanticColors.onSuccessContainer
    }

    Text(
        text = message,
        color = textColor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp)
    )
}
