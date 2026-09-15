package br.com.fiap.aguiaexpress.ui.theme

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import br.com.fiap.aguiaexpress.MainActivity
import br.com.fiap.aguiaexpress.core.settings.ThemeMode
import br.com.fiap.aguiaexpress.core.settings.ThemePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ThemeSwitchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val preferences get() = ThemePreferences(InstrumentationRegistry.getInstrumentation().targetContext)

    @Before fun resetTheme() { runBlocking { preferences.setMode(ThemeMode.DEFAULT) } }
    @After fun restoreDefaultTheme() { runBlocking { preferences.setMode(ThemeMode.DEFAULT) } }

    @Test
    fun appBarThemeChoicePersistsAndSurvivesActivityRecreation() {
        compose.onNodeWithContentDescription("Tema: Escuro").performClick()
        compose.onNodeWithText("Claro").performClick()
        compose.onNodeWithContentDescription("Tema: Claro").performClick()
        compose.onNodeWithText("Escuro").performClick()
        compose.onNodeWithContentDescription("Tema: Escuro").assertIsDisplayed()
        assertSystemBarAppearance(false)
        assertEquals(ThemeMode.DARK, runBlocking { preferences.mode.first() })
        compose.activityRule.scenario.recreate()
        compose.onNodeWithContentDescription("Tema: Escuro").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tema: Escuro").performClick()
        compose.onNodeWithText("Claro").performClick()
        compose.onNodeWithContentDescription("Tema: Claro").assertIsDisplayed()
        assertSystemBarAppearance(true)
        assertEquals(ThemeMode.LIGHT, runBlocking { preferences.mode.first() })
    }

    @Test
    fun explicitThemeCanReturnToSystemDefault() {
        compose.onNodeWithContentDescription("Tema: Escuro").performClick()
        compose.onNodeWithText("Claro").performClick()
        compose.onNodeWithContentDescription("Tema: Claro").performClick()
        compose.onNodeWithText("Sistema").performClick()
        compose.onNodeWithContentDescription("Tema: Sistema").assertIsDisplayed()
        assertEquals(ThemeMode.SYSTEM, runBlocking { preferences.mode.first() })
    }

    private fun assertSystemBarAppearance(light: Boolean) {
        compose.runOnIdle {
            val activity = compose.activity
            val controller = androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            assertEquals(light, controller.isAppearanceLightStatusBars)
            assertEquals(light, controller.isAppearanceLightNavigationBars)
        }
    }
}
