package br.com.fiap.aguiaexpress.ui.navigation

import br.com.fiap.aguiaexpress.ui.navigation.Routes.EDITAR_IDEIA

object Routes {
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

    fun editarOrientacaoRoute(orientacaoId: Long): String {
        return "$EDITAR_ORIENTACAO/$orientacaoId"
    }

    fun editarIdeiaRoute(ideiaId: Long): String {
        return "$EDITAR_IDEIA/$ideiaId"
    }
}

