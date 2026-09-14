package br.com.fiap.aguiaexpress.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeaUiAuthorizationTest {

    @Test
    fun `operational owner sees edit and delete controls`() {
        assertTrue(
            podeEditarIdeiaNoApp(
                roleAllowsEdit = true,
                loggedUserId = 17L,
                ideaOwnerId = 17L
            )
        )
    }

    @Test
    fun `operational non-owner does not see edit and delete controls`() {
        assertFalse(
            podeEditarIdeiaNoApp(
                roleAllowsEdit = true,
                loggedUserId = 18L,
                ideaOwnerId = 17L
            )
        )
    }

    @Test
    fun `role denial or missing user fails closed`() {
        assertFalse(
            podeEditarIdeiaNoApp(false, loggedUserId = 17L, ideaOwnerId = 17L)
        )
        assertFalse(
            podeEditarIdeiaNoApp(true, loggedUserId = null, ideaOwnerId = 17L)
        )
    }
}
