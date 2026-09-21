package br.com.fiap.aguiax.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiax.R
import br.com.fiap.aguiax.ui.components.AguiaButton
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import br.com.fiap.aguiax.ui.theme.semanticColors
import br.com.fiap.aguiax.viewmodel.LoginUiState
import br.com.fiap.aguiax.viewmodel.LoginViewModel

@Composable
fun LoginScreen(onLoginSuccess: (String?) -> Unit, viewModel: LoginViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess(uiState.perfil)
    }
    LoginContent(uiState, viewModel::atualizarEmail, viewModel::atualizarSenha, viewModel::login)
}

@Composable
internal fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    // Visibility is UI-only and returns to masked when this screen is recreated.
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surfaceContainer,
        unfocusedContainerColor = colors.surfaceContainer,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        focusedLabelColor = MaterialTheme.semanticColors.primaryAction,
        unfocusedLabelColor = colors.onSurfaceVariant,
        focusedLeadingIconColor = colors.onSurfaceVariant,
        unfocusedLeadingIconColor = colors.onSurfaceVariant,
        focusedTrailingIconColor = colors.onSurfaceVariant,
        unfocusedTrailingIconColor = colors.onSurfaceVariant,
        cursorColor = colors.primary
    )

    BoxWithConstraints(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                lerp(colors.background, colors.secondaryContainer, 0.10f),
                colors.background,
                colors.background
            ))
        ).imePadding()
    ) {
        val minimumHeight = maxHeight
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().heightIn(min = minimumHeight)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(R.drawable.aguiax_logo), contentDescription = "Logo AguiaX",
                    modifier = Modifier.size(176.dp), contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
                Text("Bem-vindo ao AguiaX", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold, color = colors.onBackground,
                    textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(8.dp))
                Text("Transforme ideias em resultados.", style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))

                Card(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.60f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Iniciar sessão", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold, color = colors.onSurface,
                            modifier = Modifier.semantics { heading() })
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.email, onValueChange = onEmailChange,
                            label = { Text("Email") }, placeholder = { Text("Digite seu email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(12.dp), colors = fieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.senha, onValueChange = onPasswordChange,
                            label = { Text("Senha") }, placeholder = { Text("Digite sua senha") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(48.dp)) {
                                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(12.dp), colors = fieldColors,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (!uiState.isLoading) onLogin() })
                        )
                        uiState.errorMessage?.let {
                            Spacer(Modifier.height(16.dp))
                            LoginErrorMessage(it)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                AguiaButton(
                    onClick = onLogin, enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.semanticColors.primaryAction,
                        contentColor = colors.onPrimary,
                        disabledContainerColor = colors.surfaceContainer,
                        disabledContentColor = colors.onSurfaceVariant
                    )
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isLoading) CircularProgressIndicator(
                            Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onSurfaceVariant)
                        Text(if (uiState.isLoading) "Entrando..." else "Entrar",
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginErrorMessage(message: String) {
    Text(loginErrorText(message), color = MaterialTheme.colorScheme.onErrorContainer,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .semantics { liveRegion = LiveRegionMode.Polite }.padding(12.dp))
}

// LoginUiState also carries raw exception messages. Keep technical/provider text
// out of the UI without changing repository errors or authentication behavior.
internal fun loginErrorText(message: String): String = when (message) {
    "Informe email e senha." -> message
    else -> "Não foi possível iniciar sessão. Tente novamente."
}

@Preview(showBackground = true)
@Composable
private fun LoginDarkPreview() {
    AguiaXTheme(darkTheme = true) { LoginContent(LoginUiState(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun LoginLightPreview() {
    AguiaXTheme(darkTheme = false) { LoginContent(LoginUiState(), {}, {}, {}) }
}
