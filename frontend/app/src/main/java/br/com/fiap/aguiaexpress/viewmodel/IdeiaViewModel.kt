package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.IdeiaRepository
import br.com.fiap.aguiaexpress.domain.model.Ideia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IdeiaUiState(
    val isLoading: Boolean = false,
    val ideias: List<Ideia> = emptyList(),
    val errorMessage: String? = null
)

class IdeiaViewModel : ViewModel() {

    private val repository = IdeiaRepository(
        ideiaApi = RetrofitClient.ideiaApi
    )

    private val _uiState = MutableStateFlow(IdeiaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarIdeias()
    }

    fun carregarIdeias() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.listarIdeias()
                .onSuccess { ideias ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        ideias = ideias,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar ideias"
                    )
                }
        }
    }
}