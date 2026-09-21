package br.com.fiap.aguiax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiax.core.session.SessionManager
import br.com.fiap.aguiax.data.remote.RetrofitClient
import br.com.fiap.aguiax.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val perfil: String? = null
)

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository(
        authApi = RetrofitClient.authApi
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun atualizarEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = null
        )
    }

    fun atualizarSenha(senha: String) {
        _uiState.value = _uiState.value.copy(
            senha = senha,
            errorMessage = null
        )
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank() || state.senha.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Informe email e senha."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null,
                isLoggedIn = false
            )

            authRepository.login(
                email = state.email,
                senha = state.senha
            )
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        perfil = response.perfil,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = error.message ?: "Erro ao fazer login."
                    )
                }
        }
    }

    fun logout() {
        authRepository.logout()

        _uiState.value = LoginUiState()
    }

    fun getPerfilUsuarioLogado(): String? {
        return SessionManager.perfil
    }
}