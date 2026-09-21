package br.com.fiap.aguiax.ui.screens.ideias

import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeaFeedbackColorsTest {
    @Test
    fun `success feedback is green with readable contrast in both themes`() {
        listOf(false, true).forEach { darkTheme ->
            val (background, text) = ideaSuccessColors(darkTheme)
            assertTrue(background.green > background.red && background.green > background.blue)
            assertTrue(text.green > text.red && text.green > text.blue)
            val contrast = (maxOf(background.luminance(), text.luminance()) + 0.05f) /
                (minOf(background.luminance(), text.luminance()) + 0.05f)
            assertTrue("Success text needs at least 4.5:1 contrast", contrast >= 4.5f)
        }
    }
}
