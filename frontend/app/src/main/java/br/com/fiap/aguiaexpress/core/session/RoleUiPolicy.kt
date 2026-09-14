package br.com.fiap.aguiaexpress.core.session

internal enum class ApplicationRole {
    OPERACIONAL,
    GESTOR,
    LIDER;

    companion object {
        fun from(profile: String?): ApplicationRole? {
            return entries.firstOrNull { role ->
                role.name == profile?.trim()?.uppercase()
            }
        }
    }
}

internal data class RoleUiPolicy(
    val isAuthenticated: Boolean = false,
    val canViewCategories: Boolean = false,
    val canViewOrientations: Boolean = false,
    val canManageOrientations: Boolean = false,
    val canViewIdeas: Boolean = false,
    val canCreateIdeas: Boolean = false,
    val canEditOwnIdeas: Boolean = false,
    val canVote: Boolean = false,
    val canReviewIdeas: Boolean = false,
    val canRequestAiEvaluation: Boolean = false,
    val canViewProjects: Boolean = false,
    val canManageProjects: Boolean = false,
    val canViewBadges: Boolean = false,
    val canViewDashboard: Boolean = false
)

internal fun roleUiPolicy(
    profile: String?,
    isAuthenticated: Boolean
): RoleUiPolicy {
    val role = ApplicationRole.from(profile)
    if (!isAuthenticated || role == null) {
        return RoleUiPolicy()
    }

    return RoleUiPolicy(
        isAuthenticated = true,
        canViewCategories = true,
        canViewOrientations = true,
        canManageOrientations = role == ApplicationRole.LIDER,
        canViewIdeas = true,
        canCreateIdeas = role == ApplicationRole.OPERACIONAL,
        canEditOwnIdeas = role == ApplicationRole.OPERACIONAL,
        canVote = true,
        canReviewIdeas = role == ApplicationRole.GESTOR,
        canRequestAiEvaluation = role == ApplicationRole.GESTOR,
        canViewProjects = true,
        canManageProjects = role == ApplicationRole.GESTOR,
        canViewBadges = true,
        canViewDashboard = role == ApplicationRole.LIDER
    )
}
