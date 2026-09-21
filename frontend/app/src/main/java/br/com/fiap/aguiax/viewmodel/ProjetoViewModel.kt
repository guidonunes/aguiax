package br.com.fiap.aguiax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.aguiax.data.remote.RetrofitClient
import br.com.fiap.aguiax.data.repository.ProjetoRepository
import br.com.fiap.aguiax.domain.model.Projeto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import br.com.fiap.aguiax.core.session.SessionManager
import br.com.fiap.aguiax.data.dto.CriarProjetoRequestDto
import br.com.fiap.aguiax.data.dto.EditarProjetoRequestDto
import br.com.fiap.aguiax.domain.model.Ideia
import br.com.fiap.aguiax.data.repository.IdeiaRepository

private const val ERRO_USUARIO_NAO_LOGADO = "Usuário logado não identificado."

data class ProjetoUiState(
    val isLoading: Boolean = false,
    val projetos: List<Projeto> = emptyList(),
    val errorMessage: String? = null
)

data class CriarProjetoUiState(
    val ideiaId: String = "",
    val ideias: List<Ideia> = emptyList(),
    val titulo: String = "",
    val descricao: String = "",
    val objetivo: String = "",
    val investimento: String = "",
    val roi: String = "",
    val retornoFinanceiro: String = "",
    val reducaoCustos: String = "",
    val ganhoProdutividade: String = "",
    val dataInicio: String = "",
    val dataPrevisaoFim: String = "",
    val dataConclusao: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class EditarProjetoUiState(
    val projetoId: Long? = null,
    val ideiaId: String = "",
    val ideias: List<Ideia> = emptyList(),
    val statusProjeto: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val objetivo: String = "",
    val investimento: String = "",
    val roi: String = "",
    val retornoFinanceiro: String = "",
    val reducaoCustos: String = "",
    val ganhoProdutividade: String = "",
    val dataInicio: String = "",
    val dataPrevisaoFim: String = "",
    val dataConclusao: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)



class ProjetoViewModel : ViewModel() {
    private val _criarProjetoUiState = MutableStateFlow(CriarProjetoUiState())
    val criarProjetoUiState = _criarProjetoUiState.asStateFlow()

    private val _editarProjetoUiState = MutableStateFlow(EditarProjetoUiState())
    val editarProjetoUiState = _editarProjetoUiState.asStateFlow()
    private val repository = ProjetoRepository(
        projetoApi = RetrofitClient.projetoApi
    )

    private val ideiaRepository = IdeiaRepository(
        ideiaApi = RetrofitClient.ideiaApi
    )

    private val _uiState = MutableStateFlow(ProjetoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregarProjetos()
        carregarIdeiasParaFormulario()
    }
    //CRIAR
    fun atualizarIdeiaIdCriacao(ideiaId: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(ideiaId = ideiaId)
    }

    fun atualizarTituloCriacao(titulo: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(titulo = titulo)
    }

    fun atualizarDescricaoCriacao(descricao: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(descricao = descricao)
    }

    fun atualizarObjetivoCriacao(objetivo: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(objetivo = objetivo)
    }

    fun atualizarInvestimentoCriacao(investimento: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(investimento = investimento)
    }

    fun atualizarRoiCriacao(roi: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(roi = roi)
    }

    fun atualizarRetornoFinanceiroCriacao(retornoFinanceiro: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(retornoFinanceiro = retornoFinanceiro)
    }

    fun atualizarReducaoCustosCriacao(reducaoCustos: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(reducaoCustos = reducaoCustos)
    }

    fun atualizarGanhoProdutividadeCriacao(ganhoProdutividade: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(ganhoProdutividade = ganhoProdutividade)
    }

    fun atualizarDataInicioCriacao(dataInicio: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(dataInicio = dataInicio)
    }

    fun atualizarDataPrevisaoFimCriacao(dataPrevisaoFim: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(dataPrevisaoFim = dataPrevisaoFim)
    }

    fun atualizarDataConclusaoCriacao(dataConclusao: String) {
        _criarProjetoUiState.value = _criarProjetoUiState.value.copy(dataConclusao = dataConclusao)
    }
    //EDITAR
    fun atualizarIdeiaIdEdicao(ideiaId: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(ideiaId = ideiaId)
    }

    fun atualizarTituloEdicao(titulo: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(titulo = titulo)
    }

    fun atualizarDescricaoEdicao(descricao: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(descricao = descricao)
    }

    fun atualizarObjetivoEdicao(objetivo: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(objetivo = objetivo)
    }

    fun atualizarInvestimentoEdicao(investimento: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(investimento = investimento)
    }

    fun atualizarRoiEdicao(roi: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(roi = roi)
    }

    fun atualizarRetornoFinanceiroEdicao(retornoFinanceiro: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(retornoFinanceiro = retornoFinanceiro)
    }

    fun atualizarReducaoCustosEdicao(reducaoCustos: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(reducaoCustos = reducaoCustos)
    }

    fun atualizarGanhoProdutividadeEdicao(ganhoProdutividade: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(ganhoProdutividade = ganhoProdutividade)
    }

    fun atualizarDataInicioEdicao(dataInicio: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(dataInicio = dataInicio)
    }

    fun atualizarDataPrevisaoFimEdicao(dataPrevisaoFim: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(dataPrevisaoFim = dataPrevisaoFim)
    }

    fun atualizarDataConclusaoEdicao(dataConclusao: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(dataConclusao = dataConclusao)
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

    fun criarProjeto() {
        val state = _criarProjetoUiState.value

        val ideiaId = state.ideiaId.toLongOrNull()
        val investimento = state.investimento.toDoubleOrNull()
        val roi = state.roi.toDoubleOrNull()
        val retornoFinanceiro = state.retornoFinanceiro.toDoubleOrNull()
        val reducaoCustos = state.reducaoCustos.toDoubleOrNull()
        val ganhoProdutividade = state.ganhoProdutividade.toDoubleOrNull()

        if (
            ideiaId == null ||
            state.titulo.isBlank() ||
            state.descricao.isBlank() ||
            state.objetivo.isBlank() ||
            investimento == null ||
            roi == null ||
            retornoFinanceiro == null ||
            reducaoCustos == null ||
            ganhoProdutividade == null ||
            state.dataInicio.isBlank() ||
            state.dataPrevisaoFim.isBlank()
        ) {
            _criarProjetoUiState.value = state.copy(
                errorMessage = "Preencha todos os campos obrigatórios corretamente."
            )
            return
        }

        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _criarProjetoUiState.value = state.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            _criarProjetoUiState.value = state.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            val request = CriarProjetoRequestDto(
                ideiaId = ideiaId,
                usuarioId = usuarioIdLogado,
                titulo = state.titulo,
                descricao = state.descricao,
                objetivo = state.objetivo,
                investimento = investimento,
                roi = roi,
                retornoFinanceiro = retornoFinanceiro,
                reducaoCustos = reducaoCustos,
                ganhoProdutividade = ganhoProdutividade,
                dataInicio = state.dataInicio,
                dataPrevisaoFim = state.dataPrevisaoFim,
                dataConclusao = state.dataConclusao.ifBlank { null }
            )

            repository.criarProjeto(request)
                .onSuccess {
                    _criarProjetoUiState.value = CriarProjetoUiState(
                        ideias = state.ideias,
                        successMessage = "Projeto cadastrado com sucesso!"
                    )

                    carregarProjetos()
                }
                .onFailure { error ->
                    _criarProjetoUiState.value = _criarProjetoUiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Erro ao cadastrar projeto"
                    )
                }
        }
    }

    fun atualizarStatusProjetoEdicao(statusProjeto: String) {
        _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
            statusProjeto = statusProjeto
        )
    }

    fun carregarProjetoParaEdicao(projetoId: Long) {
        viewModelScope.launch {
            _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.buscarProjetoPorId(projetoId)
                .onSuccess { projeto ->
                    _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                        projetoId = projeto.id,
                        ideiaId = projeto.ideiaId.toString(),
                        ideias = _editarProjetoUiState.value.ideias,
                        statusProjeto = projeto.statusProjeto,
                        titulo = projeto.titulo,
                        descricao = projeto.descricao,
                        objetivo = projeto.objetivo,
                        investimento = projeto.investimento.toString(),
                        roi = projeto.roi.toString(),
                        retornoFinanceiro = projeto.retornoFinanceiro.toString(),
                        reducaoCustos = projeto.reducaoCustos.toString(),
                        ganhoProdutividade = projeto.ganhoProdutividade.toString(),
                        dataInicio = projeto.dataInicio,
                        dataPrevisaoFim = projeto.dataPrevisaoFim,
                        dataConclusao = projeto.dataConclusao ?: "",
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao carregar projeto para edição"
                    )
                }
        }
    }

    fun editarProjeto() {
        val state = _editarProjetoUiState.value

        val ideiaId = state.ideiaId.toLongOrNull()
        val investimento = state.investimento.toDoubleOrNull()
        val roi = state.roi.toDoubleOrNull()
        val retornoFinanceiro = state.retornoFinanceiro.toDoubleOrNull()
        val reducaoCustos = state.reducaoCustos.toDoubleOrNull()
        val ganhoProdutividade = state.ganhoProdutividade.toDoubleOrNull()

        if (
            state.projetoId == null ||
            ideiaId == null ||
            state.titulo.isBlank() ||
            state.descricao.isBlank() ||
            state.objetivo.isBlank() ||
            investimento == null ||
            roi == null ||
            retornoFinanceiro == null ||
            reducaoCustos == null ||
            ganhoProdutividade == null ||
            state.dataInicio.isBlank() ||
            state.dataPrevisaoFim.isBlank()
        ) {
            _editarProjetoUiState.value = state.copy(
                errorMessage = "Preencha todos os campos obrigatórios corretamente."
            )
            return
        }

        val usuarioIdLogado = SessionManager.usuarioId
        if (usuarioIdLogado == null) {
            _editarProjetoUiState.value = state.copy(
                errorMessage = ERRO_USUARIO_NAO_LOGADO
            )
            return
        }

        viewModelScope.launch {
            _editarProjetoUiState.value = state.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            val request = EditarProjetoRequestDto(
                ideiaId = ideiaId,
                usuarioId = usuarioIdLogado,
                titulo = state.titulo,
                descricao = state.descricao,
                objetivo = state.objetivo,
                investimento = investimento,
                roi = roi,
                retornoFinanceiro = retornoFinanceiro,
                reducaoCustos = reducaoCustos,
                ganhoProdutividade = ganhoProdutividade,
                dataInicio = state.dataInicio,
                dataPrevisaoFim = state.dataPrevisaoFim,
                dataConclusao = state.dataConclusao.ifBlank { null }
            )

            repository.editarProjeto(
                projetoId = state.projetoId,
                request = request
            )
                .onSuccess {
                    repository.atualizarStatusProjeto(
                        projetoId = state.projetoId,
                        status = state.statusProjeto
                    )
                        .onSuccess {
                            _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                                isSaving = false,
                                successMessage = "Projeto atualizado com sucesso!",
                                errorMessage = null
                            )

                            carregarProjetos()
                        }
                        .onFailure { error ->
                            _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                                isSaving = false,
                                errorMessage = error.message ?: "Projeto editado, mas erro ao atualizar status"
                            )
                        }
                }
        }
    }

    fun deletarProjeto(projetoId: Long) {
        viewModelScope.launch {
            repository.deletarProjeto(projetoId)
                .onSuccess {
                    carregarProjetos()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao deletar projeto"
                    )
                }
        }
    }

    fun atualizarStatusProjeto(
        projetoId: Long,
        status: String
    ) {
        viewModelScope.launch {
            repository.atualizarStatusProjeto(
                projetoId = projetoId,
                status = status
            )
                .onSuccess {
                    carregarProjetos()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao atualizar status do projeto"
                    )
                }
        }
    }

    fun carregarIdeiasParaFormulario() {
        viewModelScope.launch {
            ideiaRepository.listarIdeias()
                .onSuccess { ideias ->
                    _criarProjetoUiState.value = _criarProjetoUiState.value.copy(
                        ideias = ideias
                    )

                    _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                        ideias = ideias
                    )
                }
                .onFailure { error ->
                    _criarProjetoUiState.value = _criarProjetoUiState.value.copy(
                        errorMessage = error.message ?: "Erro ao carregar ideias"
                    )

                    _editarProjetoUiState.value = _editarProjetoUiState.value.copy(
                        errorMessage = error.message ?: "Erro ao carregar ideias"
                    )
                }
        }
    }
}
