package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrientacaoUiState(
    val isLoading: Boolean = false,
    val orientacoes: List<Orientacao> = emptyList(),
    val errorMessage: String? = null
)

class OrientacaoViewModel : ViewModel() {

    private val repository = OrientacaoRepository(
        orientacaoApi = RetrofitClient.orientacaoApi
    )

    private val _uiState = MutableStateFlow(OrientacaoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarOrientacoesAtivas()
    }

    fun carregarOrientacoesAtivas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.listarOrientacoesAtivas()
                .onSuccess { orientacoes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orientacoes = orientacoes,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar orientações"
                    )
                }
        }
    }
}