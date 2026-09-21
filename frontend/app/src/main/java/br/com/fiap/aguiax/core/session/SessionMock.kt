package br.com.fiap.aguiax.core.session

object SessionMock {
    const val USUARIO_ID = 3L
    const val PERFIL_USUARIO = "LIDER"

    fun isLider(): Boolean {
        return PERFIL_USUARIO == "LIDER"
    }

    fun podeGerenciarProjetos(): Boolean {
        return PERFIL_USUARIO == "GESTOR"
    }

    fun podeAcessarDashboard(): Boolean {
        return PERFIL_USUARIO == "LIDER"
    }
}