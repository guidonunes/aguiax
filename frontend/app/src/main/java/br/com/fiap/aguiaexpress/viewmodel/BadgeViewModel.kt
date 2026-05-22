package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.session.SessionMock
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.BadgeRepository
import br.com.fiap.aguiaexpress.data.repository.UsuarioBadgeRepository
import br.com.fiap.aguiaexpress.domain.model.Badge
import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BadgeUiState(
    val isLoading: Boolean = false,
    val badgesAtivas: List<Badge> = emptyList(),
    val usuarioBadges: List<UsuarioBadge> = emptyList(),
    val errorMessage: String? = null
)

class BadgeViewModel : ViewModel() {

    private val badgeRepository = BadgeRepository(
        badgeApi = RetrofitClient.badgeApi
    )

    private val usuarioBadgeRepository = UsuarioBadgeRepository(
        usuarioBadgeApi = RetrofitClient.usuarioBadgeApi
    )

    private val _uiState = MutableStateFlow(BadgeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarMedalhas()
    }

    fun carregarMedalhas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val badgesResult = badgeRepository.listarBadgesAtivas()

            val usuarioBadgesResult = usuarioBadgeRepository.listarBadgesPorUsuario(
                usuarioId = SessionMock.USUARIO_ID
            )

            val badgesAtivas = badgesResult.getOrElse {
                emptyList()
            }

            val usuarioBadges = usuarioBadgesResult.getOrElse {
                emptyList()
            }

            val errorMessage = when {
                badgesResult.isFailure -> "Erro ao carregar medalhas disponíveis"
                usuarioBadgesResult.isFailure -> "Erro ao carregar medalhas do usuário"
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                badgesAtivas = badgesAtivas,
                usuarioBadges = usuarioBadges,
                errorMessage = errorMessage
            )
        }
    }
}