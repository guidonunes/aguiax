package br.com.fiap.aguiaexpress.viewmodel

import java.net.SocketTimeoutException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AiEvaluationErrorMessageTest {

    @Test
    fun `maps controlled backend AI status codes`() {
        val expectedMessages = mapOf(
            401 to "Sua sessão expirou. Entre novamente.",
            403 to "Apenas gestores podem solicitar uma avaliação por IA.",
            404 to "A ideia não foi encontrada.",
            502 to "O provedor de IA retornou uma avaliação inválida.",
            503 to "A avaliação por IA está temporariamente indisponível.",
            504 to "A avaliação por IA excedeu o tempo limite. Tente novamente."
        )

        expectedMessages.forEach { (status, expectedMessage) ->
            assertEquals(
                expectedMessage,
                mensagemErroAvaliacaoIa(httpException(status))
            )
        }
    }

    @Test
    fun `maps client timeout without exposing technical details`() {
        assertEquals(
            "A avaliação por IA excedeu o tempo limite. Tente novamente.",
            mensagemErroAvaliacaoIa(SocketTimeoutException("socket details"))
        )
    }

    @Test
    fun `maps malformed application response to a safe message`() {
        assertEquals(
            "A resposta da avaliação por IA é inválida.",
            mensagemErroAvaliacaoIa(
                IllegalArgumentException("provider response details")
            )
        )
    }

    private fun httpException(status: Int): HttpException {
        val body = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(status, body))
    }
}
