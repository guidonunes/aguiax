package br.com.fiap.aguiax.ui.screens.auth

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import br.com.fiap.aguiax.viewmodel.LoginUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class LoginScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loginDarkTheme() = checkLayout(true)
    @Test fun loginLightTheme() = checkLayout(false)

    @Test fun passwordVisibilityTogglesWithoutChangingCredentials() {
        show(LoginUiState(senha = "senha-teste"))
        assertPasswordMasked(true)
        compose.onNodeWithContentDescription("Mostrar senha").performClick()
        compose.onNodeWithContentDescription("Ocultar senha").assertIsDisplayed()
        assertPasswordMasked(false)
        compose.onNodeWithContentDescription("Ocultar senha").performClick()
        assertPasswordMasked(true)
    }

    @Test fun credentialsAndKeyboardActionsUseExistingCallbacks() {
        val state = mutableStateOf(LoginUiState())
        var loginCount = 0
        compose.setContent {
            AguiaXTheme { Surface {
                LoginContent(state.value, { state.value = state.value.copy(email = it) },
                    { state.value = state.value.copy(senha = it) }, { loginCount++ })
            } }
        }
        compose.onNodeWithText("Email").performTextInput("usuario@exemplo.com")
        compose.onNodeWithText("Email").performImeAction()
        compose.onNodeWithText("Senha").assertIsFocused().performTextInput("senha-teste")
        compose.onNodeWithText("Senha").performImeAction()
        compose.runOnIdle {
            assertEquals("usuario@exemplo.com", state.value.email)
            assertEquals("senha-teste", state.value.senha)
            assertEquals(1, loginCount)
        }
        compose.onNodeWithText("Entrar").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, loginCount) }
    }

    @Test fun loadingDisablesSubmissionAndShowsPortugueseLabel() {
        var loginCount = 0
        compose.setContent { AguiaXTheme { Surface {
            LoginContent(LoginUiState(isLoading = true), {}, {}, { loginCount++ })
        } } }
        compose.onNodeWithText("Entrando...").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithText("Senha").performImeAction()
        compose.runOnIdle { assertEquals(0, loginCount) }
    }

    @Test fun loginErrorIsVisibleAndAnnounced() {
        show(LoginUiState(errorMessage = "Informe email e senha."))
        compose.onNodeWithText("Informe email e senha.").performScrollTo().assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onNodeWithText("Entrar").performScrollTo().assertIsDisplayed()
    }

    @Test fun rawTransportErrorShowsPortugueseFeedback() {
        show(LoginUiState(errorMessage = "HTTP 401 Unauthorized"))
        compose.onNodeWithText("Não foi possível iniciar sessão. Tente novamente.")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("HTTP 401 Unauthorized").assertDoesNotExist()
    }

    @Test fun shortScreenAndLargeTextKeepFormAndButtonReachable() {
        compose.setContent { AguiaXTheme { Surface {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.6f)) {
                Box(Modifier.width(320.dp).height(440.dp)) {
                    LoginContent(LoginUiState(), {}, {}, {})
                }
            }
        } } }
        compose.onNodeWithText("Email").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Mostrar senha").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Entrar").performScrollTo().assertIsDisplayed()
    }

    @Test fun recreatedLoginContentReturnsPasswordToMasked() {
        val generation = mutableStateOf(0)
        compose.setContent { AguiaXTheme { Surface {
            key(generation.value) { LoginContent(LoginUiState(senha = "senha-teste"), {}, {}, {}) }
        } } }
        compose.onNodeWithContentDescription("Mostrar senha").performClick()
        assertPasswordMasked(false)
        compose.runOnIdle { generation.value++ }
        assertPasswordMasked(true)
        compose.onNodeWithContentDescription("Mostrar senha").assertIsDisplayed()
    }

    private fun assertPasswordMasked(masked: Boolean) {
        val password = "senha-teste"
        val expected = if (masked) PasswordVisualTransformation().filter(AnnotatedString(password)).text.text else password
        // Password keyboard semantics stay protected even when the eye reveals text.
        // Verify the actual rendered layout, not just the accessibility classification.
        compose.onNodeWithText("Senha").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            val layouts = mutableListOf<TextLayoutResult>()
            action(layouts)
            assertEquals(expected, layouts.single().layoutInput.text.text)
        }
    }

    private fun show(state: LoginUiState, dark: Boolean = true) {
        compose.setContent { AguiaXTheme(darkTheme = dark) { Surface {
            LoginContent(state, {}, {}, {})
        } } }
    }

    private fun checkLayout(dark: Boolean) {
        show(LoginUiState(), dark)
        compose.onNodeWithContentDescription("Logo AguiaX").assertIsDisplayed()
        compose.onNodeWithText("Bem-vindo ao AguiaX").assertIsDisplayed()
        compose.onNodeWithText("Transforme ideias em resultados.").assertIsDisplayed()
        compose.onNodeWithText("Iniciar sessão").assertIsDisplayed()
        compose.onNodeWithText("Entrar").assertIsDisplayed()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = (output?.let(::File) ?: checkNotNull(
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("login-previews")))
            .apply { mkdirs() }
        File(directory, "login-${if (dark) "dark" else "light"}.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
