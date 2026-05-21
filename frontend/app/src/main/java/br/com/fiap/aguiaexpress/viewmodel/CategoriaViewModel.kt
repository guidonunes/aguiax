package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.CategoriaRepository
import br.com.fiap.aguiaexpress.domain.model.Categoria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriaUiState(
    val isLoading: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val error: String? = null
)

class CategoriaViewModel: ViewModel() {
    private val repository = CategoriaRepository(
        categoriaApi = RetrofitClient.categoriaApi
    )

    private val _uiState = MutableStateFlow(CategoriaUiState())
    val uiState = _uiState.asStateFlow()

    fun carregarCategorias() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
                )

            repository.listarCategorias()
                .onSuccess { categorias ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = categorias,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = emptyList(),
                        error = error.message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = emptyList(),
                        error = error.message ?: "Erro ao carregar categorias"
                    )
                }
        }
    }


}