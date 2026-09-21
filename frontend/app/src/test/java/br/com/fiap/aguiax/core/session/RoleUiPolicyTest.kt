package br.com.fiap.aguiax.core.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleUiPolicyTest {

    @Test
    fun `every authenticated role receives shared read and vote capabilities`() {
        listOf("OPERACIONAL", "GESTOR", "LIDER").forEach { profile ->
            val policy = roleUiPolicy(profile, isAuthenticated = true)

            assertTrue("$profile should view categories", policy.canViewCategories)
            assertTrue("$profile should view orientations", policy.canViewOrientations)
            assertTrue("$profile should view ideas", policy.canViewIdeas)
            assertTrue("$profile should view projects", policy.canViewProjects)
            assertTrue("$profile should view badges", policy.canViewBadges)
            assertTrue("$profile should vote", policy.canVote)
        }
    }

    @Test
    fun `operational user receives only operational mutation capabilities`() {
        val policy = roleUiPolicy("OPERACIONAL", isAuthenticated = true)

        assertTrue(policy.canCreateIdeas)
        assertTrue(policy.canEditOwnIdeas)
        assertFalse(policy.canReviewIdeas)
        assertFalse(policy.canRequestAiEvaluation)
        assertFalse(policy.canManageProjects)
        assertFalse(policy.canManageOrientations)
        assertFalse(policy.canViewDashboard)
    }

    @Test
    fun `manager receives review AI and project management capabilities`() {
        val policy = roleUiPolicy("GESTOR", isAuthenticated = true)

        assertTrue(policy.canReviewIdeas)
        assertTrue(policy.canRequestAiEvaluation)
        assertTrue(policy.canManageProjects)
        assertFalse(policy.canCreateIdeas)
        assertFalse(policy.canEditOwnIdeas)
        assertFalse(policy.canManageOrientations)
        assertFalse(policy.canViewDashboard)
    }

    @Test
    fun `leader receives orientation and dashboard capabilities only`() {
        val policy = roleUiPolicy("LIDER", isAuthenticated = true)

        assertTrue(policy.canManageOrientations)
        assertTrue(policy.canViewDashboard)
        assertFalse(policy.canCreateIdeas)
        assertFalse(policy.canEditOwnIdeas)
        assertFalse(policy.canReviewIdeas)
        assertFalse(policy.canRequestAiEvaluation)
        assertFalse(policy.canManageProjects)
    }

    @Test
    fun `missing authentication and unknown profiles fail closed`() {
        val missingAuthentication = roleUiPolicy("LIDER", isAuthenticated = false)
        val unknownProfile = roleUiPolicy("ADMIN", isAuthenticated = true)

        assertFalse(missingAuthentication.isAuthenticated)
        assertFalse(missingAuthentication.canViewDashboard)
        assertFalse(missingAuthentication.canViewIdeas)
        assertFalse(unknownProfile.isAuthenticated)
        assertFalse(unknownProfile.canViewDashboard)
        assertFalse(unknownProfile.canViewIdeas)
    }
}
