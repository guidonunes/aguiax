package br.com.fiap.aguiaexpress.core.session

object SessionMock {
    const val USUARIO_ID = 2L
    const val PERFIL_USUARIO = "GESTOR"

    fun isLider(): Boolean {
        return PERFIL_USUARIO == "LIDER"
    }

    fun podeGerenciarProjetos(): Boolean {
        return PERFIL_USUARIO == "GESTOR"
    }
}