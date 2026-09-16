package br.com.fiap.aguiaexpress.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.aguiaexpress.core.session.roleUiPolicy
import br.com.fiap.aguiaexpress.ui.components.AguiaTextButton
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainRouteNavigationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var nav: NavHostController

    @Test fun gestorCanReturnHomeFromEveryMainTab() {
        showGestorGraph()
        listOf(Routes.ORIENTACOES, Routes.IDEIAS, Routes.PROJETOS, Routes.MEDALHAS).forEach { route ->
            clickRoute(route)
            clickRoute(Routes.HOME)
        }
    }

    @Test fun gestorCanReturnFromHomeShortcutAndRepeatedTabSwitches() {
        showGestorGraph()
        compose.onNodeWithText("Revisar ideias").performClick()
        compose.onNodeWithText("Tela: ${Routes.IDEIAS}").assertIsDisplayed()
        clickRoute(Routes.HOME)
        repeat(2) {
            clickRoute(Routes.IDEIAS)
            clickRoute(Routes.PROJETOS)
            clickRoute(Routes.IDEIAS)
            clickRoute(Routes.HOME)
        }
    }

    @Test fun homeDoesNotRestoreTheChildOpenedByItsShortcut() {
        showGestorGraph()
        compose.onNodeWithText("Revisar ideias").performClick()
        compose.onNodeWithText("Tela: ${Routes.IDEIAS}").assertIsDisplayed()
        // Home shortcuts navigate directly, without an earlier empty pop/save.
        // The old Home options save and immediately restore that child stack.
        compose.runOnIdle {
            nav.navigate(Routes.HOME) {
                popUpTo(Routes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        compose.runOnIdle { assertEquals(Routes.IDEIAS, nav.currentDestination?.route) }
        compose.onNodeWithText("Tela: ${Routes.IDEIAS}").assertIsDisplayed()
        clickRoute(Routes.HOME)
        compose.runOnIdle { assertEquals(Routes.HOME, nav.currentDestination?.route) }
    }

    private fun clickRoute(route: String) {
        compose.onNodeWithText("Ir para $route").performClick()
        compose.onNodeWithText("Tela: $route").assertIsDisplayed()
    }

    private fun showGestorGraph() {
        val routes = visibleMainRoutes(roleUiPolicy("GESTOR", true))
        compose.setContent { AguiaExpressTheme {
            nav = rememberNavController()
            Column {
                NavHost(nav, startDestination = Routes.LOGIN, modifier = Modifier.weight(1f)) {
                    (routes + Routes.LOGIN).forEach { route ->
                        composable(route) {
                            Column {
                                Text("Tela: $route")
                                if (route == Routes.HOME) AguiaTextButton(onClick = {
                                    nav.navigate(Routes.IDEIAS) { launchSingleTop = true }
                                }) { Text("Revisar ideias") }
                            }
                        }
                    }
                }
                routes.forEach { route ->
                    AguiaTextButton(onClick = { nav.navigateToMainRoute(route) }) { Text("Ir para $route") }
                }
            }
            LaunchedEffect(Unit) {
                // Match login's inclusive removal of the Login destination.
                nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
            }
        } }
        compose.onNodeWithText("Tela: ${Routes.HOME}").assertIsDisplayed()
    }
}
