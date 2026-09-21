package br.com.fiap.aguiax.data.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class AtualizarAvaliacaoIdeiaRequestDtoContractTest {

    private val gson = Gson()

    @Test
    fun `serializes the exact backend review field names`() {
        val request = AtualizarAvaliacaoIdeiaRequestDto(
            impacto = 8,
            viabilidade = 7
        )

        val json = gson.toJsonTree(request).asJsonObject

        assertEquals(setOf("impacto", "viabilidade"), json.keySet())
        assertEquals(8, json["impacto"].asInt)
        assertEquals(7, json["viabilidade"].asInt)
    }
}
