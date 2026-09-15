package br.com.fiap.aguiaexpress.ui.navigation

import br.com.fiap.aguiaexpress.core.session.RoleUiPolicy

internal fun visibleMainRoutes(policy: RoleUiPolicy): List<String> {
    return buildList {
        if (policy.isAuthenticated) add(Routes.HOME)
        if (policy.canViewOrientations) add(Routes.ORIENTACOES)
        if (policy.canViewIdeas) add(Routes.IDEIAS)
        if (policy.canViewProjects) add(Routes.PROJETOS)
        if (policy.canViewBadges) add(Routes.MEDALHAS)
    }
}

internal fun startRouteForRole(profile: String?): String {
    return when (profile?.trim()?.uppercase()) {
        "OPERACIONAL", "GESTOR", "LIDER" -> Routes.HOME
        else -> Routes.LOGIN
    }
}

internal fun canAccessRoute(
    route: String,
    policy: RoleUiPolicy
): Boolean {
    return when (route) {
        Routes.LOGIN -> true
        Routes.HOME -> policy.isAuthenticated
        Routes.CATEGORIAS -> policy.canViewCategories
        Routes.ORIENTACOES -> policy.canViewOrientations
        Routes.IDEIAS -> policy.canViewIdeas
        Routes.PROJETOS -> policy.canViewProjects
        Routes.MEDALHAS -> policy.canViewBadges
        Routes.DASHBOARD -> policy.canViewDashboard
        Routes.CRIAR_IDEIA -> policy.canCreateIdeas
        Routes.EDITAR_IDEIA -> policy.canEditOwnIdeas
        Routes.CRIAR_ORIENTACAO,
        Routes.EDITAR_ORIENTACAO -> policy.canManageOrientations
        Routes.CRIAR_PROJETO,
        Routes.EDITAR_PROJETO -> policy.canManageProjects
        else -> false
    }
}
