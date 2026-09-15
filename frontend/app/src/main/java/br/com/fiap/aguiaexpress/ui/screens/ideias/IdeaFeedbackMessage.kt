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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

internal fun ideaSuccessColors(darkTheme: Boolean): Pair<Color, Color> =
    if (darkTheme) {
        Color(0xFF163C23) to Color(0xFFB9F6CA)
    } else {
        Color(0xFFE8F5E9) to Color(0xFF1B5E20)
    }

@Composable
internal fun FeedbackMessage(message: String, isError: Boolean) {
    val (backgroundColor, textColor) = if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.12f) to
            MaterialTheme.colorScheme.error
    } else {
        ideaSuccessColors(MaterialTheme.colorScheme.surface.luminance() < 0.5f)
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
