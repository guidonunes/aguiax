package br.com.fiap.aguiaexpress.core.session

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
        return isOperacional()
    }

    fun podeVotarEmIdeia(): Boolean {
        return estaLogado()
    }

    fun podeEditarExcluirIdeia(): Boolean {
        return isOperacional()
    }

    fun podeAtualizarStatusIdeia(): Boolean {
        return isGestor()
    }

    fun podeGerenciarProjetos(): Boolean {
        return isGestor()
    }

    fun podeGerenciarOrientacoes(): Boolean {
        return isLider()
    }

    fun podeAcessarDashboard(): Boolean {
        return isLider()
    }

    fun podeConsultarOrientacoes(): Boolean {
        return estaLogado()
    }

    fun podeConsultarProjetos(): Boolean {
        return estaLogado()
    }

    fun podeConsultarMedalhas(): Boolean {
        return estaLogado()
    }

    fun podeAvaliarIdeia(): Boolean {
        return isGestor()
    }

    fun podeSolicitarAvaliacaoIa(): Boolean {
        return isGestor()
    }
}
