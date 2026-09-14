package br.com.fiap.aguiaexpress.ui.navigation

import br.com.fiap.aguiaexpress.core.session.roleUiPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleNavigationPolicyTest {

    private val commonRoutes = listOf(
        Routes.ORIENTACOES,
        Routes.IDEIAS,
        Routes.PROJETOS,
        Routes.MEDALHAS
    )

    @Test
    fun `main navigation exposes shared views and leader-only dashboard`() {
        assertEquals(
            commonRoutes,
            visibleMainRoutes(roleUiPolicy("OPERACIONAL", true))
        )
        assertEquals(
            commonRoutes,
            visibleMainRoutes(roleUiPolicy("GESTOR", true))
        )
        assertEquals(
            listOf(
                Routes.ORIENTACOES,
                Routes.IDEIAS,
                Routes.PROJETOS,
                Routes.DASHBOARD,
                Routes.MEDALHAS
            ),
            visibleMainRoutes(roleUiPolicy("LIDER", true))
        )
    }

    @Test
    fun `each role starts in its primary workflow`() {
        assertEquals(Routes.IDEIAS, startRouteForRole("OPERACIONAL"))
        assertEquals(Routes.PROJETOS, startRouteForRole("GESTOR"))
        assertEquals(Routes.DASHBOARD, startRouteForRole("LIDER"))
        assertEquals(Routes.LOGIN, startRouteForRole("ADMIN"))
        assertEquals(Routes.LOGIN, startRouteForRole(null))
    }

    @Test
    fun `operational routes reject manager and leader`() {
        val operational = roleUiPolicy("OPERACIONAL", true)
        val manager = roleUiPolicy("GESTOR", true)
        val leader = roleUiPolicy("LIDER", true)

        listOf(Routes.CRIAR_IDEIA, Routes.EDITAR_IDEIA).forEach { route ->
            assertTrue(canAccessRoute(route, operational))
            assertFalse(canAccessRoute(route, manager))
            assertFalse(canAccessRoute(route, leader))
        }
    }

    @Test
    fun `manager routes reject operational user and leader`() {
        val operational = roleUiPolicy("OPERACIONAL", true)
        val manager = roleUiPolicy("GESTOR", true)
        val leader = roleUiPolicy("LIDER", true)

        listOf(Routes.CRIAR_PROJETO, Routes.EDITAR_PROJETO).forEach { route ->
            assertFalse(canAccessRoute(route, operational))
            assertTrue(canAccessRoute(route, manager))
            assertFalse(canAccessRoute(route, leader))
        }
    }

    @Test
    fun `leader routes reject operational user and manager`() {
        val operational = roleUiPolicy("OPERACIONAL", true)
        val manager = roleUiPolicy("GESTOR", true)
        val leader = roleUiPolicy("LIDER", true)

        val leaderRoutes = listOf(
            Routes.CRIAR_ORIENTACAO,
            Routes.EDITAR_ORIENTACAO,
            Routes.DASHBOARD
        )
        leaderRoutes.forEach { route ->
            assertFalse(canAccessRoute(route, operational))
            assertFalse(canAccessRoute(route, manager))
            assertTrue(canAccessRoute(route, leader))
        }
    }

    @Test
    fun `unauthenticated navigation has no protected destination`() {
        val policy = roleUiPolicy("LIDER", isAuthenticated = false)

        assertTrue(visibleMainRoutes(policy).isEmpty())
        assertTrue(canAccessRoute(Routes.LOGIN, policy))
        assertFalse(canAccessRoute(Routes.IDEIAS, policy))
        assertFalse(canAccessRoute(Routes.DASHBOARD, policy))
    }
}
