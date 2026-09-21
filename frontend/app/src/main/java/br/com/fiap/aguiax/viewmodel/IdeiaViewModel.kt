package br.com.fiap.aguiax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiax.core.session.SessionManager
import br.com.fiap.aguiax.data.dto.CriarIdeiaRequestDto
import br.com.fiap.aguiax.data.remote.RetrofitClient
import br.com.fiap.aguiax.data.repository.CategoriaRepository
import br.com.fiap.aguiax.data.repository.IdeiaRepository
import br.com.fiap.aguiax.data.repository.OrientacaoRepository
import br.com.fiap.aguiax.data.repository.VotoIdeiaRepository
import br.com.fiap.aguiax.domain.model.AiIdeaEvaluation
import br.com.fiap.aguiax.domain.model.Categoria
import br.com.fiap.aguiax.domain.model.Ideia
import br.com.fiap.aguiax.domain.model.Orientacao
import br.com.fiap.aguiax.domain.model.StatusIdeia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

private const val ERRO_USUARIO_NAO_LOGADO = "Usuário logado não identificado."

internal fun ideaStatusUpdateErrorText(error: Throwable): String = when ((error as? HttpException)?.code()) {
    400 -> "A ideia mudou ou esta transição não é permitida. Atualize a lista e tente novamente."
    401 -> "Sua sessão expirou. Entre novamente."
    403 -> "Você não tem permissão para alterar o status desta ideia."
    else -> "Não foi possível atualizar o status da ideia. Tente novamente."
}

data class IdeiaUiState(
    val isLoading: Boolean = false,
    val ideias: List<Ideia> = emptyList(),
    val votosPorIdeia: Map<Long, Int> = emptyMap(),
    val ideiasVotadasPeloUsuario: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val updatingStatusIds: Set<Long> = emptySet()
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

data class AvaliacaoIdeiaUiState(
    val ideiaId: Long? = null,
    val impacto: String = "",
    val viabilidade: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class AiAvaliacaoIdeiaUiState(
    val ideiaId: Long? = null,
    val tituloIdeia: String = "",
    val isLoading: Boolean = false,
    val resultado: AiIdeaEvaluation? = null,
    val errorMessage: String? = null
)

internal data class PontuacoesAvaliacaoIdeia(
    val impacto: Int,
    val viabilidade: Int
)

internal fun validarPontuacoesAvaliacaoIdeia(
    impacto: String,
    viabilidade: String
): PontuacoesAvaliacaoIdeia? {
    val impactoNumerico = impacto.toIntOrNull()
    val viabilidadeNumerica = viabilidade.toIntOrNull()

    if (
        impactoNumerico == null ||
        viabilidadeNumerica == null ||
        impactoNumerico !in 0..10 ||
        viabilidadeNumerica !in 0..10
    ) {
        return null
    }

    return PontuacoesAvaliacaoIdeia(
        impacto = impactoNumerico,
        viabilidade = viabilidadeNumerica
    )
}

internal fun mensagemErroAvaliacaoIa(error: Throwable): String {
    if (error is SocketTimeoutException) {
        return "A avaliação por IA excedeu o tempo limite. Tente novamente."
    }

    if (error is HttpException) {
        return when (error.code()) {
            401 -> "Sua sessão expirou. Entre novamente."
            403 -> "Apenas gestores podem solicitar uma avaliação por IA."
            404 -> "A ideia não foi encontrada."
            502 -> "O provedor de IA retornou uma avaliação inválida."
            503 -> "A avaliação por IA está temporariamente indisponível."
            504 -> "A avaliação por IA excedeu o tempo limite. Tente novamente."
            else -> "Não foi possível avaliar a ideia por IA."
        }
    }

    if (error is IllegalArgumentException) {
        return "A resposta da avaliação por IA é inválida."
    }

    return "Não foi possível avaliar a ideia por IA."
}


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

    private val _avaliacaoIdeiaUiState =
        MutableStateFlow(AvaliacaoIdeiaUiState())

    val avaliacaoIdeiaUiState = _avaliacaoIdeiaUiState.asStateFlow()

    private val _aiAvaliacaoIdeiaUiState =
        MutableStateFlow(AiAvaliacaoIdeiaUiState())

    val aiAvaliacaoIdeiaUiState = _aiAvaliacaoIdeiaUiState.asStateFlow()


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

            val usuarioIdLogado = SessionManager.usuarioId
            if (usuarioIdLogado == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ERRO_USUARIO_NAO_LOGADO
                )
                return@launch
            }

            repository.listarIdeias()
                .onSuccess { ideias ->
                    val votosPorIdeia = buscarQuantidadeVotosPorIdeia(ideias)

                    val votosUsuario = votoIdeiaRepository
                        .listarVotosPorUsuario(usuarioIdLogado)
                        .getOrElse { emptyList() }

                    val ideiasVotadasPeloUsuario = votosUsuario
                        .map { voto -> voto.ideiaId }
                        .toSet()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        ideias = ordenarIdeiasMaisRecentes(ideias),
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

        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _criarIdeiaUiState.value = state.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
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
                usuarioId = usuarioIdLogado,
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
        if (ideiaId in _uiState.value.updatingStatusIds) return
        _uiState.value = _uiState.value.copy(
            updatingStatusIds = _uiState.value.updatingStatusIds + ideiaId,
            errorMessage = null
        )
        viewModelScope.launch {
            try {
                repository.atualizarStatus(ideiaId = ideiaId, status = status.name)
                    .onSuccess { updated ->
                        _uiState.value = _uiState.value.copy(
                            ideias = _uiState.value.ideias.map { if (it.id == ideiaId) updated else it }
                        )
                        carregarIdeias()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(errorMessage = ideaStatusUpdateErrorText(error))
                    }
            } finally {
                _uiState.value = _uiState.value.copy(
                    updatingStatusIds = _uiState.value.updatingStatusIds - ideiaId
                )
            }
        }
    }

    fun votarIdeia(ideiaId: Long) {
        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            votoIdeiaRepository.votarIdeia(
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
        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            votoIdeiaRepository.removerVotoIdeia(
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
        val usuarioIdLogado = SessionManager.usuarioId

        if (usuarioIdLogado == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            if (usuarioJaVotou) {
                votoIdeiaRepository.removerVotoIdeia(
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

        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _editarIdeiaUiState.value = state.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
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
                usuarioId = usuarioIdLogado,
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

    fun abrirAvaliacaoIdeia(ideia: Ideia) {
        _avaliacaoIdeiaUiState.value = AvaliacaoIdeiaUiState(
            ideiaId = ideia.id,
            impacto = ideia.impacto?.toString().orEmpty(),
            viabilidade = ideia.viabilidade?.toString().orEmpty()
        )
    }

    fun fecharAvaliacaoIdeia() {
        if (_avaliacaoIdeiaUiState.value.isSaving) {
            return
        }

        _avaliacaoIdeiaUiState.value = AvaliacaoIdeiaUiState()
    }

    fun atualizarImpactoAvaliacao(impacto: String) {
        _avaliacaoIdeiaUiState.value =
            _avaliacaoIdeiaUiState.value.copy(
                impacto = impacto,
                errorMessage = null
            )
    }

    fun atualizarViabilidadeAvaliacao(viabilidade: String) {
        _avaliacaoIdeiaUiState.value =
            _avaliacaoIdeiaUiState.value.copy(
                viabilidade = viabilidade,
                errorMessage = null
            )
    }

    fun salvarAvaliacaoIdeia() {
        val state = _avaliacaoIdeiaUiState.value

        if (!SessionManager.podeAvaliarIdeia()) {
            _avaliacaoIdeiaUiState.value = state.copy(
                errorMessage = "Apenas gestores podem avaliar ideias."
            )
            return
        }

        val ideiaId = state.ideiaId
        val pontuacoes = validarPontuacoesAvaliacaoIdeia(
            impacto = state.impacto,
            viabilidade = state.viabilidade
        )

        if (ideiaId == null || pontuacoes == null) {
            _avaliacaoIdeiaUiState.value = state.copy(
                errorMessage = "Impacto e viabilidade devem estar entre 0 e 10."
            )
            return
        }

        viewModelScope.launch {
            _avaliacaoIdeiaUiState.value = state.copy(
                isSaving = true,
                errorMessage = null
            )

            repository.atualizarAvaliacao(
                ideiaId = ideiaId,
                impacto = pontuacoes.impacto,
                viabilidade = pontuacoes.viabilidade
            )
                .onSuccess { ideiaAtualizada ->
                    _uiState.value = _uiState.value.copy(
                        ideias = _uiState.value.ideias.map { ideia ->
                            if (ideia.id == ideiaAtualizada.id) {
                                ideiaAtualizada
                            } else {
                                ideia
                            }
                        }
                    )

                    _avaliacaoIdeiaUiState.value =
                        AvaliacaoIdeiaUiState()
                }
                .onFailure { error ->
                    _avaliacaoIdeiaUiState.value =
                        _avaliacaoIdeiaUiState.value.copy(
                            isSaving = false,
                            errorMessage = error.message
                                ?: "Erro ao salvar a avaliação da ideia."
                        )
                }
        }
    }

    fun solicitarAvaliacaoIa(ideia: Ideia) {
        iniciarAvaliacaoIa(
            ideiaId = ideia.id,
            tituloIdeia = ideia.titulo
        )
    }

    fun repetirAvaliacaoIa() {
        val state = _aiAvaliacaoIdeiaUiState.value
        val ideiaId = state.ideiaId ?: return

        iniciarAvaliacaoIa(
            ideiaId = ideiaId,
            tituloIdeia = state.tituloIdeia
        )
    }

    fun fecharAvaliacaoIa() {
        if (_aiAvaliacaoIdeiaUiState.value.isLoading) {
            return
        }

        _aiAvaliacaoIdeiaUiState.value = AiAvaliacaoIdeiaUiState()
    }

    private fun iniciarAvaliacaoIa(
        ideiaId: Long,
        tituloIdeia: String
    ) {
        if (_aiAvaliacaoIdeiaUiState.value.isLoading) {
            return
        }

        if (!SessionManager.podeSolicitarAvaliacaoIa()) {
            _aiAvaliacaoIdeiaUiState.value = AiAvaliacaoIdeiaUiState(
                ideiaId = ideiaId,
                tituloIdeia = tituloIdeia,
                errorMessage = "Apenas gestores podem solicitar uma avaliação por IA."
            )
            return
        }

        _aiAvaliacaoIdeiaUiState.value = AiAvaliacaoIdeiaUiState(
            ideiaId = ideiaId,
            tituloIdeia = tituloIdeia,
            isLoading = true
        )

        viewModelScope.launch {
            val evaluationResult = repository.avaliarIdeiaComIa(ideiaId)

            if (evaluationResult.isFailure) {
                val error = checkNotNull(evaluationResult.exceptionOrNull())
                _aiAvaliacaoIdeiaUiState.value =
                    _aiAvaliacaoIdeiaUiState.value.copy(
                        isLoading = false,
                        resultado = null,
                        errorMessage = mensagemErroAvaliacaoIa(error)
                    )
                return@launch
            }

            val evaluation = evaluationResult.getOrThrow()
            _aiAvaliacaoIdeiaUiState.value =
                _aiAvaliacaoIdeiaUiState.value.copy(
                    isLoading = false,
                    resultado = evaluation,
                    errorMessage = null
                )

            repository.buscarIdeiaPorId(ideiaId)
                .onSuccess { ideiaAtualizada ->
                    _uiState.value = _uiState.value.copy(
                        ideias = _uiState.value.ideias.map { ideia ->
                            if (ideia.id == ideiaAtualizada.id) {
                                ideiaAtualizada
                            } else {
                                ideia
                            }
                        }
                    )
                }
        }
    }
}
