package br.com.fiap.aguiax.viewmodel

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DashboardErrorMessageTest {

    @Test
    fun `maps dashboard authorization and missing-orientation responses`() {
        val expectedMessages = mapOf(
            401 to "Sua sessão expirou. Entre novamente.",
            403 to "O dashboard executivo é exclusivo para líderes.",
            404 to "A orientação estratégica selecionada não foi encontrada."
        )

        expectedMessages.forEach { (status, expectedMessage) ->
            assertEquals(
                expectedMessage,
                mensagemErroDashboard(httpException(status))
            )
        }
    }

    @Test
    fun `does not expose invalid response details`() {
        assertEquals(
            "O backend retornou um resumo de dashboard inválido.",
            mensagemErroDashboard(
                IllegalArgumentException("internal contract details")
            )
        )
    }

    private fun httpException(status: Int): HttpException {
        val body = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(status, body))
    }
}
