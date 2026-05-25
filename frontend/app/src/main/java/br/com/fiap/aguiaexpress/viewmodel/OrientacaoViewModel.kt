package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.session.SessionManager
import br.com.fiap.aguiaexpress.data.dto.CriarOrientacaoRequestDto
import br.com.fiap.aguiaexpress.data.dto.EditarOrientacaoRequestDto
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val ERRO_USUARIO_NAO_LOGADO = "Usuário logado não identificado."

data class OrientacaoUiState(
    val isLoading: Boolean = false,
    val orientacoes: List<Orientacao> = emptyList(),
    val errorMessage: String? = null
)

data class CriarOrientacaoUiState(
    val titulo: String = "",
    val descricao: String = "",
    val areaEstrategica: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class EditarOrientacaoUiState(
    val orientacaoId: Long? = null,
    val titulo: String = "",
    val descricao: String = "",
    val areaEstrategica: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class OrientacaoViewModel : ViewModel() {

    private val repository = OrientacaoRepository(
        orientacaoApi = RetrofitClient.orientacaoApi
    )

    private val _uiState = MutableStateFlow(OrientacaoUiState())
    val uiState = _uiState.asStateFlow()

    private val _criarOrientacaoUiState = MutableStateFlow(CriarOrientacaoUiState())
    val criarOrientacaoUiState = _criarOrientacaoUiState.asStateFlow()

    private val _editarOrientacaoUiState = MutableStateFlow(EditarOrientacaoUiState())
    val editarOrientacaoUiState = _editarOrientacaoUiState.asStateFlow()

    init {
        carregarOrientacoes()
    }

    fun carregarOrientacoes() {
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

    fun atualizarTituloCriacao(titulo: String) {
        _criarOrientacaoUiState.value = _criarOrientacaoUiState.value.copy(titulo = titulo)
    }

    fun atualizarDescricaoCriacao(descricao: String) {
        _criarOrientacaoUiState.value = _criarOrientacaoUiState.value.copy(descricao = descricao)
    }

    fun atualizarAreaEstrategicaCriacao(areaEstrategica: String) {
        _criarOrientacaoUiState.value = _criarOrientacaoUiState.value.copy(areaEstrategica = areaEstrategica)
    }

    fun criarOrientacao() {
        val state = _criarOrientacaoUiState.value

        if (
            state.titulo.isBlank() ||
            state.descricao.isBlank() ||
            state.areaEstrategica.isBlank()
        ) {
            _criarOrientacaoUiState.value = state.copy(
                errorMessage = "Preencha todos os campos."
            )
            return
        }

        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _criarOrientacaoUiState.value = state.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            _criarOrientacaoUiState.value = state.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            val request = CriarOrientacaoRequestDto(
                usuarioId = usuarioIdLogado,
                titulo = state.titulo,
                descricao = state.descricao,
                areaEstrategica = state.areaEstrategica
            )

            repository.criarOrientacao(request)
                .onSuccess {
                    _criarOrientacaoUiState.value = CriarOrientacaoUiState(
                        successMessage = "Orientação cadastrada com sucesso!"
                    )

                    carregarOrientacoes()
                }
                .onFailure { error ->
                    _criarOrientacaoUiState.value = _criarOrientacaoUiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Erro ao cadastrar orientação"
                    )
                }
        }
    }

    fun carregarOrientacaoParaEdicao(orientacaoId: Long) {
        viewModelScope.launch {
            _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.buscarOrientacaoPorId(orientacaoId)
                .onSuccess { orientacao ->
                    _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(
                        orientacaoId = orientacao.id,
                        titulo = orientacao.titulo,
                        descricao = orientacao.descricao,
                        areaEstrategica = orientacao.areaEstrategica,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar orientação para edição"
                    )
                }
        }
    }

    fun atualizarTituloEdicao(titulo: String) {
        _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(titulo = titulo)
    }

    fun atualizarDescricaoEdicao(descricao: String) {
        _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(descricao = descricao)
    }

    fun atualizarAreaEstrategicaEdicao(areaEstrategica: String) {
        _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(areaEstrategica = areaEstrategica)
    }

    fun editarOrientacao() {
        val state = _editarOrientacaoUiState.value

        if (
            state.orientacaoId == null ||
            state.titulo.isBlank() ||
            state.descricao.isBlank() ||
            state.areaEstrategica.isBlank()
        ) {
            _editarOrientacaoUiState.value = state.copy(
                errorMessage = "Preencha todos os campos."
            )
            return
        }

        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _editarOrientacaoUiState.value = state.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            _editarOrientacaoUiState.value = state.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            val request = EditarOrientacaoRequestDto(
                usuarioId = usuarioIdLogado,
                titulo = state.titulo,
                descricao = state.descricao,
                areaEstrategica = state.areaEstrategica
            )

            repository.editarOrientacao(
                orientacaoId = state.orientacaoId,
                request = request
            )
                .onSuccess {
                    _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(
                        isSaving = false,
                        successMessage = "Orientação atualizada com sucesso!",
                        errorMessage = null
                    )

                    carregarOrientacoes()
                }
                .onFailure { error ->
                    _editarOrientacaoUiState.value = _editarOrientacaoUiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Erro ao atualizar orientação"
                    )
                }
        }
    }

    fun deletarOrientacao(orientacaoId: Long) {
        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            repository.deletarOrientacao(
                orientacaoId = orientacaoId,
                usuarioId = usuarioIdLogado
            )
                .onSuccess {
                    carregarOrientacoes()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao deletar orientação"
                    )
                }
        }
    }
}
