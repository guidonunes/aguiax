package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.session.SessionManager
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.BadgeRepository
import br.com.fiap.aguiaexpress.data.repository.UsuarioBadgeRepository
import br.com.fiap.aguiaexpress.domain.model.Badge
import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ERRO_USUARIO_NAO_LOGADO = "Usuário logado não identificado."

data class BadgeUiState(
    val isLoading: Boolean = false,
    val badgesDisponiveis: List<Badge> = emptyList(),
    val usuarioBadges: List<UsuarioBadge> = emptyList(),
    val totalBadgesAtivas: Int = 0,
    val totalBadgesAtivasConquistadas: Int = 0,
    val errorMessage: String? = null
)

internal data class ResumoCatalogoBadges(
    val badgesDisponiveis: List<Badge>,
    val totalBadgesAtivas: Int,
    val totalBadgesAtivasConquistadas: Int
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

            val usuarioIdLogado = SessionManager.usuarioId
            if (usuarioIdLogado == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ERRO_USUARIO_NAO_LOGADO
                )
                return@launch
            }

            val badgesResult = badgeRepository.listarBadgesAtivas()

            val usuarioBadgesResult = usuarioBadgeRepository.listarBadgesPorUsuario(
                usuarioId = usuarioIdLogado
            )

            val badgesAtivas = badgesResult.getOrElse {
                emptyList()
            }

            val usuarioBadges = usuarioBadgesResult.getOrElse {
                emptyList()
            }

            val resumoCatalogo = calcularResumoCatalogoBadges(
                badgesAtivas = badgesAtivas,
                usuarioBadges = usuarioBadges
            )

            val errorMessage = when {
                badgesResult.isFailure -> "Erro ao carregar medalhas disponíveis"
                usuarioBadgesResult.isFailure -> "Erro ao carregar medalhas do usuário"
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                badgesDisponiveis = resumoCatalogo.badgesDisponiveis,
                usuarioBadges = usuarioBadges,
                totalBadgesAtivas = resumoCatalogo.totalBadgesAtivas,
                totalBadgesAtivasConquistadas =
                    resumoCatalogo.totalBadgesAtivasConquistadas,
                errorMessage = errorMessage
            )
        }
    }
}

internal fun calcularResumoCatalogoBadges(
    badgesAtivas: List<Badge>,
    usuarioBadges: List<UsuarioBadge>
): ResumoCatalogoBadges {
    val badgeIdsConquistadas = usuarioBadges
        .mapTo(mutableSetOf()) { usuarioBadge ->
            usuarioBadge.badgeId
        }

    val badgesDisponiveis = badgesAtivas.filterNot { badge ->
        badge.id in badgeIdsConquistadas
    }

    val totalBadgesAtivasConquistadas = badgesAtivas
        .map { badge -> badge.id }
        .toSet()
        .count { badgeId -> badgeId in badgeIdsConquistadas }

    return ResumoCatalogoBadges(
        badgesDisponiveis = badgesDisponiveis,
        totalBadgesAtivas = badgesAtivas.size,
        totalBadgesAtivasConquistadas = totalBadgesAtivasConquistadas
    )
}
