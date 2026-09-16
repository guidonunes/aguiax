package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.data.dto.IdeiaDto
import br.com.fiap.aguiaexpress.data.remote.api.IdeiaApi
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal

class IdeiaRepositoryStatusContractTest {
    @Test fun `real Retrofit sends the documented analysis prioritization and approval requests`() = runBlocking {
        val backend = StrictStatusTransport()
        val repository = IdeiaRepository(backend.api)
        listOf("EM_ANALISE", "PRIORIZADA", "APROVADA").forEach { status ->
            assertEquals(status, repository.atualizarStatus(42, status).getOrThrow().status)
        }
        assertEquals(listOf("EM_ANALISE", "PRIORIZADA", "APROVADA"), backend.statuses)
        assertEquals("APROVADA", backend.persistedStatus)
    }

    @Test fun `backend rejection of direct approval is preserved without a silent intermediate transition`() = runBlocking {
        val backend = StrictStatusTransport()
        val result = IdeiaRepository(backend.api).atualizarStatus(42, "APROVADA")
        assertTrue(result.isFailure)
        assertEquals(400, (result.exceptionOrNull() as HttpException).code())
        assertEquals(listOf("APROVADA"), backend.statuses)
        assertEquals("NOVA", backend.persistedStatus)
    }

    // Intercept HTTP locally; exercise Retrofit/Gson without a live API or new dependencies.
    private class StrictStatusTransport {
        var persistedStatus = "NOVA"
        val statuses = mutableListOf<String>()
        private val gson = Gson()
        private val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            assertEquals("PATCH", request.method)
            assertEquals("/ideias/42/status", request.url.encodedPath)
            val buffer = Buffer()
            checkNotNull(request.body).writeTo(buffer)
            val payload = gson.fromJson(buffer.readUtf8(), Map::class.java)
            assertEquals(setOf("status"), payload.keys)
            val target = payload["status"] as String
            statuses += target
            val allowed = when (persistedStatus) {
                "NOVA" -> target == "EM_ANALISE"
                "EM_ANALISE" -> target in setOf("PRIORIZADA", "APROVADA", "REJEITADA")
                "PRIORIZADA" -> target in setOf("APROVADA", "REJEITADA")
                else -> false
            }
            if (allowed) persistedStatus = target
            val body = if (allowed) gson.toJson(IdeiaDto(42, 5, 3, 9, "Ideia", "Descrição", "Problema",
                "Solução", 0, 0, BigDecimal.ZERO, statusIdeia = persistedStatus)) else "{\"message\":\"Transição inválida\"}"
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(if (allowed) 200 else 400).message(if (allowed) "OK" else "Bad Request")
                .body(body.toResponseBody()).build()
        }.build()
        val api: IdeiaApi = Retrofit.Builder().baseUrl("https://api.example.test/")
            .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(IdeiaApi::class.java)
    }
}
