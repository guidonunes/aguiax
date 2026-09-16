package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.session.SessionManager
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.DashboardRepository
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.domain.model.DashboardInsight
import br.com.fiap.aguiaexpress.domain.model.DashboardSummary
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val orientacoes: List<Orientacao> = emptyList(),
    val orientacaoSelecionadaId: Long? = null,
    val errorMessage: String? = null
)

data class DashboardInsightUiState(
    val isLoading: Boolean = false,
    val insight: DashboardInsight? = null,
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

internal fun mensagemErroInsightDashboard(error: Throwable): String {
    if (error is SocketTimeoutException) {
        return "A análise executiva excedeu o tempo limite. Tente novamente."
    }

    if (error is HttpException) {
        return when (error.code()) {
            400 -> "Não há projetos neste recorte para gerar uma análise."
            401 -> "Sua sessão expirou. Entre novamente."
            403 -> "A análise executiva é exclusiva para líderes."
            404 -> "A orientação estratégica selecionada não foi encontrada."
            502 -> "O provedor de IA retornou uma análise inválida."
            503 -> "A análise executiva está temporariamente indisponível."
            504 -> "A análise executiva excedeu o tempo limite. Tente novamente."
            else -> "Não foi possível gerar a análise executiva."
        }
    }

    if (error is IllegalArgumentException) {
        return "O backend retornou uma análise executiva inválida."
    }

    return "Não foi possível gerar a análise executiva."
}

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository = DashboardRepository(
        dashboardApi = RetrofitClient.dashboardApi
    ),
    private val orientacaoRepository: OrientacaoRepository = OrientacaoRepository(
        orientacaoApi = RetrofitClient.orientacaoApi
    ),
    private val canRequestInsight: () -> Boolean = {
        SessionManager.podeAcessarDashboard()
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private val _insightUiState = MutableStateFlow(DashboardInsightUiState())
    val insightUiState = _insightUiState.asStateFlow()

    private var orientacoesCarregadas = false
    private var requestSequence = 0L
    private var insightRequestSequence = 0L

    init {
        carregarDashboard()
    }

    fun carregarDashboard() {
        invalidarInsight()
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

    fun gerarInsight() {
        val state = _uiState.value
        if (_insightUiState.value.isLoading) {
            return
        }

        if (!canRequestInsight()) {
            invalidarInsight()
            _insightUiState.value = DashboardInsightUiState(
                errorMessage = "A análise executiva é exclusiva para líderes."
            )
            return
        }

        val summary = state.summary
        if (summary == null || summary.totalProjects == 0L) {
            invalidarInsight()
            _insightUiState.value = DashboardInsightUiState(
                errorMessage = "Não há projetos neste recorte para gerar uma análise."
            )
            return
        }

        val selectedOrientationId = state.orientacaoSelecionadaId
        val requestId = ++insightRequestSequence
        _insightUiState.value = DashboardInsightUiState(isLoading = true)

        viewModelScope.launch {
            dashboardRepository.gerarInsight(selectedOrientationId)
                .onSuccess { insight ->
                    if (
                        requestId != insightRequestSequence ||
                        selectedOrientationId !=
                        _uiState.value.orientacaoSelecionadaId
                    ) {
                        return@onSuccess
                    }
                    if (!canRequestInsight()) {
                        invalidarInsight()
                        return@onSuccess
                    }

                    _uiState.value = _uiState.value.copy(
                        summary = insight.metricsSnapshot,
                        errorMessage = null
                    )
                    _insightUiState.value = DashboardInsightUiState(
                        insight = insight
                    )
                }
                .onFailure { error ->
                    if (
                        requestId == insightRequestSequence &&
                        selectedOrientationId ==
                        _uiState.value.orientacaoSelecionadaId
                    ) {
                        if (canRequestInsight()) {
                            _insightUiState.value = DashboardInsightUiState(
                                errorMessage = mensagemErroInsightDashboard(error)
                            )
                        } else {
                            invalidarInsight()
                        }
                    }
                }
        }
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

    private fun invalidarInsight() {
        insightRequestSequence++
        _insightUiState.value = DashboardInsightUiState()
    }
}
