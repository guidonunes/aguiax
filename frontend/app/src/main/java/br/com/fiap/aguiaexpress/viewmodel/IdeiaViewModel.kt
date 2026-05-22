package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.session.SessionMock
import br.com.fiap.aguiaexpress.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.CategoriaRepository
import br.com.fiap.aguiaexpress.data.repository.IdeiaRepository
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.domain.model.Categoria
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IdeiaUiState(
    val isLoading: Boolean = false,
    val ideias: List<Ideia> = emptyList(),
    val errorMessage: String? = null
)

data class CriarIdeiaUiState(
    val titulo: String = "",
    val descricao: String = "",
    val problema: String = "",
    val solucaoProposta: String = "",
    val categoriaId: Long? = null,
    val orientacaoId: Long? = null,
    val categorias: List<Categoria> = emptyList(),
    val orientacoes: List<Orientacao> = emptyList(),
    val isLoadingOptions: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class IdeiaViewModel : ViewModel() {

    private val repository = IdeiaRepository(
        ideiaApi = RetrofitClient.ideiaApi
    )

    private val categoriaRepository = CategoriaRepository(
        categoriaApi = RetrofitClient.categoriaApi
    )

    private val orientacaoRepository = OrientacaoRepository(
        orientacaoApi = RetrofitClient.orientacaoApi
    )

    private val _uiState = MutableStateFlow(IdeiaUiState())
    val uiState = _uiState.asStateFlow()

    private val _criarIdeiaUiState = MutableStateFlow(CriarIdeiaUiState())
    val criarIdeiaUiState = _criarIdeiaUiState.asStateFlow()

    init {
        carregarIdeias()
        carregarOpcoesFormulario()
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

    fun atualizarTitulo(titulo: String) {
        _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(titulo = titulo)
    }

    fun atualizarDescricao(descricao: String) {
        _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(descricao = descricao)
    }

    fun atualizarProblema(problema: String) {
        _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(problema = problema)
    }

    fun atualizarSolucaoProposta(solucaoProposta: String) {
        _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(solucaoProposta = solucaoProposta)
    }

    fun atualizarCategoriaId(categoriaId: Long?) {
        _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(categoriaId = categoriaId)
    }

    fun atualizarOrientacaoId(orientacaoId: Long?) {
        _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(orientacaoId = orientacaoId)
    }

    fun criarIdeia() {
        val state = _criarIdeiaUiState.value

        if (
            state.titulo.isBlank() ||
            state.descricao.isBlank() ||
            state.problema.isBlank() ||
            state.solucaoProposta.isBlank() ||
            state.categoriaId == null ||
            state.orientacaoId == null
        ) {
            _criarIdeiaUiState.value = state.copy(
                errorMessage = "Preencha todos os campos."
            )
            return
        }

        viewModelScope.launch {
            _criarIdeiaUiState.value = state.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            val request = CriarIdeiaRequestDto(
                usuarioId = SessionMock.USUARIO_ID,
                categoriaId = state.categoriaId,
                orientacaoId = state.orientacaoId,
                titulo = state.titulo,
                descricao = state.descricao,
                problema = state.problema,
                solucaoProposta = state.solucaoProposta
            )

            repository.criarIdeia(request)
                .onSuccess {
                    _criarIdeiaUiState.value = CriarIdeiaUiState(
                        successMessage = "Ideia cadastrada com sucesso!"
                    )

                    carregarIdeias()
                }
                .onFailure { error ->
                    _criarIdeiaUiState.value = state.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Erro ao cadastrar ideia"
                    )
                }
        }
    }

    fun carregarOpcoesFormulario() {
        viewModelScope.launch {
            _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(
                isLoadingOptions = true,
                errorMessage = null
            )

            val categoriasResult = categoriaRepository.listarCategorias()
            val orientacoesResult = orientacaoRepository.listarOrientacoesAtivas()

            val categorias = categoriasResult.getOrElse { emptyList() }
            val orientacoes = orientacoesResult.getOrElse { emptyList() }

            val errorMessage = when {
                categoriasResult.isFailure -> "Erro ao carregar categorias"
                orientacoesResult.isFailure -> "Erro ao carregar orientações"
                else -> null
            }

            _criarIdeiaUiState.value = _criarIdeiaUiState.value.copy(
                isLoadingOptions = false,
                categorias = categorias,
                orientacoes = orientacoes,
                errorMessage = errorMessage
            )
        }
    }
}