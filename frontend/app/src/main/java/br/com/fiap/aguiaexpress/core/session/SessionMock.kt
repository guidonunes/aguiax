package br.com.fiap.aguiaexpress.core.session

object SessionMock {
    const val USUARIO_ID = 3L
    const val PERFIL_USUARIO = "LIDER"

    fun isLider(): Boolean {
        return PERFIL_USUARIO == "LIDER"
    }
}