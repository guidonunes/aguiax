package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.DashboardRepository
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.domain.model.DashboardSummary
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val orientacoes: List<Orientacao> = emptyList(),
    val orientacaoSelecionadaId: Long? = null,
    val errorMessage: String? = null
)

internal fun mensagemErroDashboard(error: Throwable): String {
    if (error is HttpException) {
        return when (error.code()) {
            401 -> "Sua sessão expirou. Entre novamente."
            403 -> "O dashboard executivo é exclusivo para líderes."
            404 -> "A orientação estratégica selecionada não foi encontrada."
            else -> "Não foi possível carregar o dashboard."
        }
    }

    if (error is IllegalArgumentException) {
        return "O backend retornou um resumo de dashboard inválido."
    }

    return "Não foi possível carregar o dashboard."
}

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository = DashboardRepository(
        dashboardApi = RetrofitClient.dashboardApi
    ),
    private val orientacaoRepository: OrientacaoRepository = OrientacaoRepository(
        orientacaoApi = RetrofitClient.orientacaoApi
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private var orientacoesCarregadas = false
    private var requestSequence = 0L

    init {
        carregarDashboard()
    }

    fun carregarDashboard() {
        val requestId = ++requestSequence
        val selectedOrientationId = _uiState.value.orientacaoSelecionadaId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                summary = null,
                errorMessage = null
            )

            if (!orientacoesCarregadas) {
                val orientationsResult = orientacaoRepository.listarOrientacoes()
                if (requestId != requestSequence) {
                    return@launch
                }

                if (orientationsResult.isFailure) {
                    publicarErro(
                        requestId,
                        checkNotNull(orientationsResult.exceptionOrNull())
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    orientacoes = orientationsResult.getOrThrow()
                        .sortedBy { orientation -> orientation.titulo }
                )
                orientacoesCarregadas = true
            }

            dashboardRepository.buscarResumo(selectedOrientationId)
                .onSuccess { summary ->
                    if (requestId == requestSequence) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            summary = summary,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    publicarErro(requestId, error)
                }
        }
    }

    fun selecionarOrientacao(orientationId: Long?) {
        val state = _uiState.value
        if (state.orientacaoSelecionadaId == orientationId) {
            return
        }
        if (
            orientationId != null &&
            state.orientacoes.none { orientation -> orientation.id == orientationId }
        ) {
            return
        }

        _uiState.value = state.copy(
            orientacaoSelecionadaId = orientationId,
            summary = null,
            errorMessage = null
        )
        carregarDashboard()
    }

    private fun publicarErro(requestId: Long, error: Throwable) {
        if (requestId != requestSequence) {
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            summary = null,
            errorMessage = mensagemErroDashboard(error)
        )
    }
}
