package br.com.fiap.aguiax.ui.navigation

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiax.core.settings.ThemeMode
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppTopBarTest {
    @get:Rule val compose = createComposeRule()

    @Test fun wordmarkAndActionsInLightTheme() = verifyTopBar(false)
    @Test fun wordmarkAndActionsInDarkTheme() = verifyTopBar(true)

    private fun verifyTopBar(dark: Boolean) {
        var logoutCount = 0
        var selectedMode: ThemeMode? = null
        compose.setContent {
            AguiaXTheme(darkTheme = dark) {
                Surface {
                    AppTopBar(themeMode = ThemeMode.SYSTEM,
                        onThemeModeChange = { selectedMode = it },
                        onLogoutClick = { logoutCount++ })
                }
            }
        }
        compose.onNodeWithContentDescription("AguiaX").assertIsDisplayed()
        compose.onNodeWithText("AguiaX").assertDoesNotExist()
        compose.onNodeWithText("Bem-vindo,").assertDoesNotExist()
        compose.onNodeWithContentDescription("Tema: Sistema").performClick()
        compose.onNodeWithText("Escuro").performClick()
        compose.runOnIdle { assertEquals(ThemeMode.DARK, selectedMode) }
        compose.onNodeWithContentDescription("Sair").performClick()
        compose.runOnIdle { assertEquals(1, logoutCount) }
    }
}
