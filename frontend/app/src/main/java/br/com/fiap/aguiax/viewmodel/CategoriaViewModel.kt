package br.com.fiap.aguiax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiax.data.remote.RetrofitClient
import br.com.fiap.aguiax.data.repository.CategoriaRepository
import br.com.fiap.aguiax.domain.model.Categoria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriaUiState(
    val isLoading: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val errorMessage: String? = null
)

class CategoriaViewModel: ViewModel() {
    private val repository = CategoriaRepository(
        categoriaApi = RetrofitClient.categoriaApi
    )

    private val _uiState = MutableStateFlow(CategoriaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarCategorias()
    }
    fun carregarCategorias() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
                )

            repository.listarCategorias()
                .onSuccess { categorias ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = categorias,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = emptyList(),
                        errorMessage = error.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = emptyList(),
                        errorMessage = error.message ?: "Erro ao carregar categorias"
                    )
                }
        }
    }


}