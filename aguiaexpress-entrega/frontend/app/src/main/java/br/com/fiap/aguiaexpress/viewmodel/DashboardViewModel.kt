package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.ProjetoRepository
import br.com.fiap.aguiaexpress.domain.model.Projeto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val projetos: List<Projeto> = emptyList(),

    val totalProjetos: Int = 0,
    val projetosPorStatus: Map<String, Int> = emptyMap(),

    val investimentoTotal: Double = 0.0,
    val retornoFinanceiroTotal: Double = 0.0,
    val reducaoCustosTotal: Double = 0.0,
    val resultadoEstimado: Double = 0.0,

    val roiMedio: Double = 0.0,
    val ganhoProdutividadeMedio: Double = 0.0,

    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val projetoRepository = ProjetoRepository(
        projetoApi = RetrofitClient.projetoApi
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarDashboard()
    }

    fun carregarDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            projetoRepository.listarProjetos()
                .onSuccess { projetos ->
                    _uiState.value = calcularResumoProjetos(projetos)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar dashboard"
                    )
                }
        }
    }

    private fun calcularResumoProjetos(
        projetos: List<Projeto>
    ): DashboardUiState {
        val totalProjetos = projetos.size

        val projetosPorStatus = projetos
            .groupingBy { projeto ->
                projeto.statusProjeto
            }
            .eachCount()

        val investimentoTotal = projetos.sumOf { projeto ->
            projeto.investimento
        }

        val retornoFinanceiroTotal = projetos.sumOf { projeto ->
            projeto.retornoFinanceiro
        }

        val reducaoCustosTotal = projetos.sumOf { projeto ->
            projeto.reducaoCustos
        }

        val resultadoEstimado = retornoFinanceiroTotal + reducaoCustosTotal - investimentoTotal

        val roiMedio = if (projetos.isNotEmpty()) {
            projetos.map { projeto ->
                projeto.roi
            }.average()
        } else {
            0.0
        }

        val ganhoProdutividadeMedio = if (projetos.isNotEmpty()) {
            projetos.map { projeto ->
                projeto.ganhoProdutividade
            }.average()
        } else {
            0.0
        }

        return DashboardUiState(
            isLoading = false,
            projetos = projetos,
            totalProjetos = totalProjetos,
            projetosPorStatus = projetosPorStatus,
            investimentoTotal = investimentoTotal,
            retornoFinanceiroTotal = retornoFinanceiroTotal,
            reducaoCustosTotal = reducaoCustosTotal,
            resultadoEstimado = resultadoEstimado,
            roiMedio = roiMedio,
            ganhoProdutividadeMedio = ganhoProdutividadeMedio,
            errorMessage = null
        )
    }
}