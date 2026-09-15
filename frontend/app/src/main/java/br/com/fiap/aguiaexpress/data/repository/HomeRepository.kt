package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.data.remote.RetrofitClient
import br.com.fiap.aguiaexpress.domain.model.DashboardSummary
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.domain.model.Projeto
import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge
import br.com.fiap.aguiaexpress.domain.model.HomeInputs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

internal interface HomeDataSource {
    suspend fun ideas(userId: Long? = null): Result<List<Ideia>>
    suspend fun badges(userId: Long): Result<List<UsuarioBadge>>
    suspend fun projects(): Result<List<Projeto>>
    suspend fun orientations(): Result<List<Orientacao>>
    suspend fun summary(orientationId: Long? = null): Result<DashboardSummary>
}

internal class RepositoryHomeDataSource : HomeDataSource {
    private val ideas = IdeiaRepository(RetrofitClient.ideiaApi)
    private val badges = UsuarioBadgeRepository(RetrofitClient.usuarioBadgeApi)
    private val projects = ProjetoRepository(RetrofitClient.projetoApi)
    private val orientations = OrientacaoRepository(RetrofitClient.orientacaoApi)
    private val dashboard = DashboardRepository(RetrofitClient.dashboardApi)

    override suspend fun ideas(userId: Long?) = if (userId == null) ideas.listarIdeias() else ideas.listarIdeiasPorUsuario(userId)
    override suspend fun badges(userId: Long) = badges.listarBadgesPorUsuario(userId)
    override suspend fun projects() = projects.listarProjetos()
    override suspend fun orientations() = orientations.listarOrientacoes()
    override suspend fun summary(orientationId: Long?) = dashboard.buscarResumo(orientationId)
}

internal class HomeRepository(private val source: HomeDataSource = RepositoryHomeDataSource()) {
    suspend fun load(role: ApplicationRole, userId: Long): HomeInputs = coroutineScope {
        require(userId > 0) { "Sessão sem usuário válido" }
        val warnings = mutableListOf<String>()
        when (role) {
            ApplicationRole.OPERACIONAL -> {
                val ideas = async { source.ideas(userId) }
                val badges = async { source.badges(userId) }
                val projects = async { source.projects() }
                HomeInputs(
                    ideas = available(ideas.await(), "suas ideias", warnings),
                    badges = available(badges.await(), "suas medalhas", warnings),
                    projects = available(projects.await(), "os projetos", warnings),
                    warnings = warnings.toList()
                )
            }
            ApplicationRole.GESTOR -> {
                val ideas = async { source.ideas() }
                val projects = async { source.projects() }
                HomeInputs(
                    ideas = available(ideas.await(), "as ideias", warnings),
                    projects = available(projects.await(), "os projetos", warnings),
                    warnings = warnings.toList()
                )
            }
            ApplicationRole.LIDER -> {
                // Executive aggregates always come from the backend, never from downloaded projects.
                val summary = async { source.summary() }
                val orientations = async { source.orientations() }
                val loadedSummary = available(summary.await(), "o resumo executivo", warnings)
                val loadedOrientations = available(orientations.await(), "as orientações", warnings)
                val filtered = loadedOrientations.orEmpty().sortedBy { it.titulo }.take(3).map { orientation ->
                    async { orientation to source.summary(orientation.id) }
                }.awaitAll()
                val strategySummaries = filtered.mapNotNull { (orientation, result) ->
                    available(result, "os projetos de ${orientation.titulo}", warnings)?.let { orientation.id to it }
                }.toMap()
                HomeInputs(
                    summary = loadedSummary,
                    orientations = loadedOrientations,
                    strategySummaries = strategySummaries,
                    warnings = warnings.toList()
                )
            }
        }
    }

    private fun <T> available(result: Result<T>, label: String, warnings: MutableList<String>): T? {
        val error = result.exceptionOrNull()
        if (error is CancellationException || error is HttpException && error.code() == 401) throw error
        if (error != null) warnings += "Não foi possível carregar $label."
        return result.getOrNull()
    }
}
