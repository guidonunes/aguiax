package br.com.fiap.aguiax.ui.navigation

import br.com.fiap.aguiax.core.session.roleUiPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleNavigationPolicyTest {

    private val commonRoutes = listOf(
        Routes.HOME,
        Routes.ORIENTACOES,
        Routes.IDEIAS,
        Routes.PROJETOS,
        Routes.MEDALHAS
    )

    @Test
    fun `all roles share five main destinations with a shared home`() {
        assertEquals(
            commonRoutes,
            visibleMainRoutes(roleUiPolicy("OPERACIONAL", true))
        )
        assertEquals(
            commonRoutes,
            visibleMainRoutes(roleUiPolicy("GESTOR", true))
        )
        assertEquals(
            commonRoutes,
            visibleMainRoutes(roleUiPolicy("LIDER", true))
        )
    }

    @Test
    fun `each role starts in the shared home`() {
        assertEquals(Routes.HOME, startRouteForRole("OPERACIONAL"))
        assertEquals(Routes.HOME, startRouteForRole("GESTOR"))
        assertEquals(Routes.HOME, startRouteForRole("LIDER"))
        assertEquals(Routes.HOME, startRouteForRole(" lider "))
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
        assertFalse(canAccessRoute(Routes.HOME, policy))
    }

    @Test
    fun `unknown role cannot access home or any main route`() {
        val policy = roleUiPolicy("ADMIN", true)
        assertTrue(visibleMainRoutes(policy).isEmpty())
        assertFalse(canAccessRoute(Routes.HOME, policy))
        assertFalse(canAccessRoute(Routes.DASHBOARD, policy))
    }
}
