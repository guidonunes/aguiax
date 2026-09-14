package br.com.fiap.aguiaexpress.core.session

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionManagerIdeaReviewTest {

    @After
    fun clearSession() {
        SessionManager.limparSessao()
    }

    @Test
    fun `only manager receives the idea review capability`() {
        listOf("OPERACIONAL", "GESTOR", "LIDER").forEach { role ->
            SessionManager.salvarSessao(
                token = "test-token",
                usuarioId = 17L,
                nome = "Usuário de teste",
                email = "teste@aguiaexpress.com",
                perfil = role
            )

            assertEquals(
                "Unexpected idea-review permission for $role",
                role == "GESTOR",
                SessionManager.podeAvaliarIdeia()
            )
            assertEquals(
                "Unexpected AI-evaluation permission for $role",
                role == "GESTOR",
                SessionManager.podeSolicitarAvaliacaoIa()
            )
        }
    }
}
