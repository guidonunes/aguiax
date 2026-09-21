package br.com.fiap.aguiax.viewmodel

import java.net.SocketTimeoutException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DashboardInsightErrorMessageTest {

    @Test
    fun `maps every controlled backend insight status to pt-BR`() {
        val expectedMessages = mapOf(
            400 to "Não há projetos neste recorte para gerar uma análise.",
            401 to "Sua sessão expirou. Entre novamente.",
            403 to "A análise executiva é exclusiva para líderes.",
            404 to "A orientação estratégica selecionada não foi encontrada.",
            502 to "O provedor de IA retornou uma análise inválida.",
            503 to "A análise executiva está temporariamente indisponível.",
            504 to "A análise executiva excedeu o tempo limite. Tente novamente."
        )

        expectedMessages.forEach { (status, message) ->
            assertEquals(
                message,
                mensagemErroInsightDashboard(httpException(status))
            )
        }
    }

    @Test
    fun `maps client timeout without exposing technical details`() {
        assertEquals(
            "A análise executiva excedeu o tempo limite. Tente novamente.",
            mensagemErroInsightDashboard(
                SocketTimeoutException("internal host and timeout")
            )
        )
    }

    @Test
    fun `maps malformed application response without exposing validation details`() {
        assertEquals(
            "O backend retornou uma análise executiva inválida.",
            mensagemErroInsightDashboard(
                IllegalArgumentException("internal response details")
            )
        )
    }

    private fun httpException(status: Int): HttpException {
        val body = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(status, body))
    }
}
