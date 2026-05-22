package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.ProjetoRepository
import br.com.fiap.aguiaexpress.domain.model.Projeto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjetoUiState(
    val isLoading: Boolean = false,
    val projetos: List<Projeto> = emptyList(),
    val errorMessage: String? = null
)

class ProjetoViewModel : ViewModel() {

    private val repository = ProjetoRepository(
        projetoApi = RetrofitClient.projetoApi
    )

    private val _uiState = MutableStateFlow(ProjetoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarProjetos()
    }

    fun carregarProjetos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.listarProjetos()
                .onSuccess { projetos ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        projetos = projetos,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar projetos"
                    )
                }
        }
    }
}