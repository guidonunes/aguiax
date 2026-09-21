package br.com.fiap.aguiax.core.session

object SessionManager {

    var token: String? = null
        private set

    var usuarioId: Long? = null
        private set

    var nome: String? = null
        private set

    var email: String? = null
        private set

    var perfil: String? = null
        private set

    fun salvarSessao(
        token: String,
        usuarioId: Long,
        nome: String,
        email: String,
        perfil: String
    ) {
        this.token = token
        this.usuarioId = usuarioId
        this.nome = nome
        this.email = email
        this.perfil = perfil
    }

    fun limparSessao() {
        token = null
        usuarioId = null
        nome = null
        email = null
        perfil = null
    }

    fun estaLogado(): Boolean {
        return !token.isNullOrBlank()
    }

    fun isOperacional(): Boolean {
        return perfil == "OPERACIONAL"
    }

    fun isGestor(): Boolean {
        return perfil == "GESTOR"
    }

    fun isLider(): Boolean {
        return perfil == "LIDER"
    }

    fun podeCadastrarIdeia(): Boolean {
        return uiPolicy().canCreateIdeas
    }

    fun podeVotarEmIdeia(): Boolean {
        return uiPolicy().canVote
    }

    fun podeEditarExcluirIdeia(): Boolean {
        return uiPolicy().canEditOwnIdeas
    }

    fun podeAtualizarStatusIdeia(): Boolean {
        return uiPolicy().canReviewIdeas
    }

    fun podeGerenciarProjetos(): Boolean {
        return uiPolicy().canManageProjects
    }

    fun podeGerenciarOrientacoes(): Boolean {
        return uiPolicy().canManageOrientations
    }

    fun podeAcessarDashboard(): Boolean {
        return uiPolicy().canViewDashboard
    }

    fun podeConsultarCategorias(): Boolean {
        return uiPolicy().canViewCategories
    }

    fun podeConsultarOrientacoes(): Boolean {
        return uiPolicy().canViewOrientations
    }

    fun podeConsultarIdeias(): Boolean {
        return uiPolicy().canViewIdeas
    }

    fun podeConsultarProjetos(): Boolean {
        return uiPolicy().canViewProjects
    }

    fun podeConsultarMedalhas(): Boolean {
        return uiPolicy().canViewBadges
    }

    fun podeAvaliarIdeia(): Boolean {
        return uiPolicy().canReviewIdeas
    }

    fun podeSolicitarAvaliacaoIa(): Boolean {
        return uiPolicy().canRequestAiEvaluation
    }

    internal fun uiPolicy(): RoleUiPolicy {
        return roleUiPolicy(
            profile = perfil,
            isAuthenticated = estaLogado()
        )
    }
}
