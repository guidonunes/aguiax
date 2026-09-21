package br.com.fiap.aguiax.viewmodel

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class IdeaStatusUpdateErrorTextTest {
    @Test fun `bad transitions tell the user to refresh rather than showing HTTP 400`() {
        val error = HttpException(Response.error<Unit>(400, "".toResponseBody()))
        assertEquals("A ideia mudou ou esta transição não é permitida. Atualize a lista e tente novamente.",
            ideaStatusUpdateErrorText(error))
    }
    @Test fun `authentication authorization and other errors have safe Portuguese messages`() {
        mapOf(401 to "Sua sessão expirou. Entre novamente.",
            403 to "Você não tem permissão para alterar o status desta ideia.",
            500 to "Não foi possível atualizar o status da ideia. Tente novamente.").forEach { (code, message) ->
            assertEquals(message, ideaStatusUpdateErrorText(HttpException(Response.error<Unit>(code, "".toResponseBody()))))
        }
        assertEquals("Não foi possível atualizar o status da ideia. Tente novamente.",
            ideaStatusUpdateErrorText(IllegalStateException("Transport error")))
    }
}
