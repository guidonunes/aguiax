package br.com.fiap.aguiaexpress.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"

    const val CATEGORIAS = "categorias"
    const val ORIENTACOES = "orientacoes"
    const val IDEIAS = "ideias"
    const val CRIAR_IDEIA = "criar_ideia"
    const val EDITAR_IDEIA = "editar_ideia"
    const val PROJETOS = "projetos"
    const val MEDALHAS = "medalhas"

    const val CRIAR_ORIENTACAO = "criar_orientacao"
    const val EDITAR_ORIENTACAO = "editar_orientacao"

    const val CRIAR_PROJETO = "criar_projeto"
    const val EDITAR_PROJETO = "editar_projeto"

    const val DASHBOARD = "dashboard"

    fun editarOrientacaoRoute(orientacaoId: Long): String {
        return "$EDITAR_ORIENTACAO/$orientacaoId"
    }

    fun editarIdeiaRoute(ideiaId: Long): String {
        return "$EDITAR_IDEIA/$ideiaId"
    }

    fun editarProjetoRoute(projetoId: Long): String {
        return "$EDITAR_PROJETO/$projetoId"
    }
}
