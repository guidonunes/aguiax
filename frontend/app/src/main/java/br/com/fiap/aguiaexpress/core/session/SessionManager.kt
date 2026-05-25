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

    fun podeGerenciarOrientacoes(): Boolean {
        return isLider()
    }

    fun podeGerenciarProjetos(): Boolean {
        return isGestor()
    }

    fun podeAcessarDashboard(): Boolean {
        return isLider()
    }
}