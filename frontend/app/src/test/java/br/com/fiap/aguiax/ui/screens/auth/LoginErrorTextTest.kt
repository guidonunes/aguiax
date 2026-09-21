package br.com.fiap.aguiax.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class LoginErrorTextTest {
    @Test fun `existing required credential message remains unchanged`() {
        assertEquals("Informe email e senha.", loginErrorText("Informe email e senha."))
    }

    @Test fun `raw transport messages become safe Portuguese feedback`() {
        listOf("HTTP 401 Unauthorized", "HTTP 500 Internal Server Error",
            "Unable to resolve host api.example.test", "Failed to connect", "Erro ao fazer login.", "")
            .forEach { message ->
                assertEquals("Não foi possível iniciar sessão. Tente novamente.", loginErrorText(message))
            }
    }
}
