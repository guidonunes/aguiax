package br.com.fiap.aguiax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiax.core.session.ApplicationRole
import br.com.fiap.aguiax.core.session.SessionManager
import br.com.fiap.aguiax.data.repository.HomeRepository
import br.com.fiap.aguiax.ui.screens.home.HomeOverview
import br.com.fiap.aguiax.ui.screens.home.buildHomeOverview
import br.com.fiap.aguiax.ui.screens.home.homeGreeting
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalTime

internal data class HomeUiState(
    val greeting: String = "",
    val overview: HomeOverview? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

internal class HomeViewModel(private val repository: HomeRepository = HomeRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    // Loaded on destination RESUME, including return from creation/editing and bottom navigation.
    fun refresh() {
        loadJob?.cancel()
        val token = SessionManager.token
        val userId = SessionManager.usuarioId
        val role = ApplicationRole.from(SessionManager.perfil)
        val greeting = homeGreeting(SessionManager.nome, LocalTime.now().hour)
        if (token.isNullOrBlank() || userId == null || userId <= 0 || role == null) {
            _uiState.value = HomeUiState(greeting, isLoading = false, errorMessage = "Entre novamente para acessar o início.")
            return
        }
        _uiState.value = HomeUiState(greeting = greeting)
        loadJob = viewModelScope.launch {
            try {
                val inputs = repository.load(role, userId)
                if (!sameSession(token, userId, role)) return@launch
                _uiState.value = HomeUiState(greeting, buildHomeOverview(role, userId, inputs), isLoading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (!sameSession(token, userId, role)) return@launch
                val message = when {
                    error is HttpException && error.code() == 401 -> "Sua sessão expirou. Entre novamente."
                    error is HttpException && error.code() == 403 -> "Seu perfil não possui acesso a este resumo."
                    else -> "Não foi possível carregar o início. Tente novamente."
                }
                _uiState.value = HomeUiState(greeting, isLoading = false, errorMessage = message)
            }
        }
    }

    private fun sameSession(token: String, userId: Long, role: ApplicationRole): Boolean =
        SessionManager.token == token && SessionManager.usuarioId == userId &&
            ApplicationRole.from(SessionManager.perfil) == role
}
