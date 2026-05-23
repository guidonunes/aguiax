package br.com.fiap.aguiaexpress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiaexpress.core.session.SessionMock
import br.com.fiap.aguiaexpress.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.data.repository.CategoriaRepository
import br.com.fiap.aguiaexpress.data.repository.IdeiaRepository
import br.com.fiap.aguiaexpress.data.repository.OrientacaoRepository
import br.com.fiap.aguiaexpress.data.repository.VotoIdeiaRepository
import br.com.fiap.aguiaexpress.domain.model.Categoria
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.domain.model.StatusIdeia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IdeiaUiState(
    val isLoading: Boolean = false,
    val ideias: List<Ideia> = emptyList(),
    val votosPorIdeia: Map<Long, Int> = emptyMap(),
    val ideiasVotadasPeloUsuario: Set<Long> = emptySet(),
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

data class EditarIdeiaUiState(
    val ideiaId: Long? = null,
    val titulo: String = "",
    val descricao: String = "",
    val problema: String = "",
    val solucaoProposta: String = "",
    val categoriaId: Long? = null,
    val orientacaoId: Long? = null,
    val categorias: List<Categoria> = emptyList(),
    val orientacoes: List<Orientacao> = emptyList(),
    val isLoading: Boolean = false,
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

    private val _editarIdeiaUiState = MutableStateFlow(EditarIdeiaUiState())
    val editarIdeiaUiState = _editarIdeiaUiState.asStateFlow()

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
                    val votosPorIdeia = buscarQuantidadeVotosPorIdeia(ideias)

                    val votosUsuario = votoIdeiaRepository
                        .listarVotosPorUsuario(SessionMock.USUARIO_ID)
                        .getOrElse { emptyList() }

                    val ideiasVotadasPeloUsuario = votosUsuario
                        .map { voto -> voto.ideiaId }
                        .toSet()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        ideias = ideias,
                        votosPorIdeia = votosPorIdeia,
                        ideiasVotadasPeloUsuario = ideiasVotadasPeloUsuario,
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

    fun atualizarTituloEdicao(titulo: String) {
        _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(titulo = titulo)
    }

    fun atualizarDescricaoEdicao(descricao: String) {
        _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(descricao = descricao)
    }

    fun atualizarProblemaEdicao(problema: String) {
        _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(problema = problema)
    }

    fun atualizarSolucaoPropostaEdicao(solucaoProposta: String) {
        _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(solucaoProposta = solucaoProposta)
    }

    fun atualizarCategoriaIdEdicao(categoriaId: Long?) {
        _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(categoriaId = categoriaId)
    }

    fun atualizarOrientacaoIdEdicao(orientacaoId: Long?) {
        _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(orientacaoId = orientacaoId)
    }

    private val votoIdeiaRepository = VotoIdeiaRepository(
        votoIdeiaApi = RetrofitClient.votoIdeiaApi
    )
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

    fun atualizarStatusIdeia(
        ideiaId: Long,
        status: StatusIdeia
    ) {
        viewModelScope.launch {
            repository.atualizarStatus(
                ideiaId = ideiaId,
                status = status.name
            )
                .onSuccess {
                    carregarIdeias()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao atualizar status da ideia"
                    )
                }
        }
    }

    fun votarIdeia(ideiaId: Long) {
        viewModelScope.launch {
            votoIdeiaRepository.votarIdeia(
                usuarioId = SessionMock.USUARIO_ID,
                ideiaId = ideiaId
            )
                .onSuccess {
                    carregarIdeias()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao votar na ideia"
                    )
                }
        }
    }

    fun removerVotoIdeia(ideiaId: Long) {
        viewModelScope.launch {
            votoIdeiaRepository.removerVotoIdeia(
                usuarioId = SessionMock.USUARIO_ID,
                ideiaId = ideiaId
            )
                .onSuccess {
                    carregarIdeias()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao remover voto da ideia"
                    )
                }
        }
    }

    private suspend fun buscarQuantidadeVotosPorIdeia(
        ideias: List<Ideia>
    ): Map<Long, Int> {
        val votosMap = mutableMapOf<Long, Int>()

        ideias.forEach { ideia ->
            votoIdeiaRepository.listarVotosPorIdeia(ideia.id)
                .onSuccess { votos ->
                    votosMap[ideia.id] = votos.size
                }
                .onFailure {
                    votosMap[ideia.id] = 0
                }
        }

        return votosMap
    }

    fun alternarVotoIdeia(ideiaId: Long) {
        val usuarioJaVotou = _uiState.value.ideiasVotadasPeloUsuario.contains(ideiaId)

        viewModelScope.launch {
            if (usuarioJaVotou) {
                votoIdeiaRepository.removerVotoIdeia(
                    usuarioId = SessionMock.USUARIO_ID,
                    ideiaId = ideiaId
                )
                    .onSuccess {
                        carregarIdeias()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Erro ao remover voto"
                        )
                    }
            } else {
                votoIdeiaRepository.votarIdeia(
                    usuarioId = SessionMock.USUARIO_ID,
                    ideiaId = ideiaId
                )
                    .onSuccess {
                        carregarIdeias()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Erro ao votar na ideia"
                        )
                    }
            }
        }
    }

    fun deletarIdeia(ideiaId: Long) {
        viewModelScope.launch {
            repository.deletarIdeia(ideiaId)
                .onSuccess {
                    carregarIdeias()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao deletar ideia"
                    )
                }
        }
    }

    fun carregarIdeiaParaEdicao(ideiaId: Long) {
        viewModelScope.launch {
            _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val ideiaResult = repository.buscarIdeiaPorId(ideiaId)
            val categoriasResult = categoriaRepository.listarCategorias()
            val orientacoesResult = orientacaoRepository.listarOrientacoesAtivas()

            val categorias = categoriasResult.getOrElse { emptyList() }
            val orientacoes = orientacoesResult.getOrElse { emptyList() }

            ideiaResult
                .onSuccess { ideia ->
                    _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(
                        ideiaId = ideia.id,
                        titulo = ideia.titulo,
                        descricao = ideia.descricao,
                        problema = ideia.problema,
                        solucaoProposta = ideia.solucaoProposta,
                        categoriaId = ideia.categoriaId,
                        orientacaoId = ideia.orientacaoId,
                        categorias = categorias,
                        orientacoes = orientacoes,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(
                        isLoading = false,
                        categorias = categorias,
                        orientacoes = orientacoes,
                        errorMessage = error.message ?: "Erro ao carregar ideia para edição"
                    )
                }
        }
    }

    fun editarIdeia() {
        val state = _editarIdeiaUiState.value

        if (
            state.ideiaId == null ||
            state.titulo.isBlank() ||
            state.descricao.isBlank() ||
            state.problema.isBlank() ||
            state.solucaoProposta.isBlank() ||
            state.categoriaId == null ||
            state.orientacaoId == null
        ) {
            _editarIdeiaUiState.value = state.copy(
                errorMessage = "Preencha todos os campos."
            )
            return
        }

        viewModelScope.launch {
            _editarIdeiaUiState.value = state.copy(
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

            repository.editarIdeia(
                ideiaId = state.ideiaId,
                request = request
            )
                .onSuccess {
                    _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(
                        isSaving = false,
                        successMessage = "Ideia atualizada com sucesso!",
                        errorMessage = null
                    )

                    carregarIdeias()
                }
                .onFailure { error ->
                    _editarIdeiaUiState.value = _editarIdeiaUiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Erro ao atualizar ideia"
                    )
                }
        }
    }
}