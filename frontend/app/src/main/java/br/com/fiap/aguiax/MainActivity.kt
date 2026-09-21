package br.com.fiap.aguiax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.aguiax.viewmodel.ThemeViewModel
import br.com.fiap.aguiax.core.settings.ThemeMode
import androidx.compose.ui.Modifier
import br.com.fiap.aguiax.ui.navigation.AppNavigation
import br.com.fiap.aguiax.ui.theme.AguiaXTheme

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by themeViewModel.state.collectAsStateWithLifecycle()
            val mode = state.mode
            val systemDark = isSystemInDarkTheme()
            val darkTheme = (mode ?: ThemeMode.DEFAULT).usesDarkTheme(systemDark)
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            AguiaXTheme(darkTheme = darkTheme) {
                // Wait for the stored mode before creating navigation, avoiding a light-theme flash.
                if (mode == null) Surface(Modifier.fillMaxSize()) {} else {
                    AppNavigation(themeMode = mode, isThemeSaving = state.isSaving,
                        themeError = state.errorMessage, onThemeModeChange = themeViewModel::selectMode,
                        onThemeErrorDismiss = themeViewModel::dismissError)
                }
            }
        }
    }
}
