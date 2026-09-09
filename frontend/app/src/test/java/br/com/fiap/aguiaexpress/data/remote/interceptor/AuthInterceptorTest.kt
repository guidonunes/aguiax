package br.com.fiap.aguiaexpress.data.remote.interceptor

import br.com.fiap.aguiaexpress.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class AuthInterceptorTest {

    @After
    fun tearDown() {
        SessionManager.limparSessao()
    }

    @Test
    fun requestWithoutSessionDoesNotReceiveAuthorizationHeader() {
        SessionManager.limparSessao()

        val result = executeRequest()

        assertNull(result.request.header("Authorization"))
        assertEquals("", result.standardOutput)
    }

    @Test
    fun authenticatedRequestReceivesBearerTokenWithoutLoggingIt() {
        val token = "sensitive-test-jwt"
        saveSession(token)

        val result = executeRequest()

        assertEquals("Bearer $token", result.request.header("Authorization"))
        assertFalse(result.standardOutput.contains(token))
        assertFalse(result.standardOutput.contains("Authorization"))
    }

    @Test
    fun interceptorReplacesExistingAuthorizationHeaderInsteadOfDuplicatingIt() {
        val token = "current-session-jwt"
        saveSession(token)
        val originalRequest = Request.Builder()
            .url("http://localhost/protected")
            .addHeader("Authorization", "Bearer stale-jwt")
            .build()

        val result = executeRequest(originalRequest)

        assertEquals(listOf("Bearer $token"), result.request.headers("Authorization"))
        assertFalse(result.standardOutput.contains(token))
        assertFalse(result.standardOutput.contains("stale-jwt"))
    }

    private fun executeRequest(
        originalRequest: Request = Request.Builder()
            .url("http://localhost/protected")
            .build()
    ): ExecutionResult {
        var interceptedRequest: Request? = null
        val terminalInterceptor = Interceptor { chain ->
            interceptedRequest = chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody())
                .build()
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(terminalInterceptor)
            .build()
        val output = ByteArrayOutputStream()
        val originalOutput = System.out

        try {
            System.setOut(PrintStream(output))
            client.newCall(originalRequest).execute().use { response ->
                assertEquals(200, response.code)
            }
        } finally {
            System.setOut(originalOutput)
        }

        return ExecutionResult(
            request = requireNotNull(interceptedRequest),
            standardOutput = output.toString()
        )
    }

    private fun saveSession(token: String) {
        SessionManager.salvarSessao(
            token = token,
            usuarioId = 7L,
            nome = "Test user",
            email = "test@example.com",
            perfil = "OPERACIONAL"
        )
    }

    private data class ExecutionResult(
        val request: Request,
        val standardOutput: String
    )
}
