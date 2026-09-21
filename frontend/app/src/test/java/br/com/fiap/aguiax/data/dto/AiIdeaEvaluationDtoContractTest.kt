package br.com.fiap.aguiax.data.dto

import com.google.gson.Gson
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class AiIdeaEvaluationDtoContractTest {

    private val gson = Gson()

    @Test
    fun `maps the complete structured AI response`() {
        val dto = gson.fromJson(validResponse(), AiIdeaEvaluationDto::class.java)

        val result = dto.toDomain()

        assertEquals(42L, result.ideaId)
        assertEquals(9, result.strategicAlignment)
        assertEquals(8, result.impact)
        assertEquals(7, result.feasibility)
        assertEquals(8, result.innovation)
        assertEquals(BigDecimal("8.1"), result.overallScore)
        assertEquals("Resumo validado.", result.summary)
    }

    @Test
    fun `rejects missing and out of range AI scores`() {
        val invalidResponses = listOf(
            validResponse().replace("\"strategicAlignment\": 9,", ""),
            validResponse().replace("\"impact\": 8", "\"impact\": 11"),
            validResponse().replace("\"feasibility\": 7", "\"feasibility\": -1"),
            validResponse().replace("\"overallScore\": 8.1", "\"overallScore\": 10.1")
        )

        invalidResponses.forEach { json ->
            val dto = gson.fromJson(json, AiIdeaEvaluationDto::class.java)

            assertThrows(IllegalArgumentException::class.java) {
                dto.toDomain()
            }
        }
    }

    @Test
    fun `trims summary and rejects a blank summary`() {
        val validDto = gson.fromJson(
            validResponse().replace(
                "Resumo validado.",
                "  Resumo validado.  "
            ),
            AiIdeaEvaluationDto::class.java
        )
        assertEquals("Resumo validado.", validDto.toDomain().summary)

        val blankDto = gson.fromJson(
            validResponse().replace("Resumo validado.", "   "),
            AiIdeaEvaluationDto::class.java
        )
        assertThrows(IllegalArgumentException::class.java) {
            blankDto.toDomain()
        }
    }

    @Test
    fun `AI response contract contains no workflow decision fields`() {
        val json = gson.toJsonTree(
            gson.fromJson(validResponse(), AiIdeaEvaluationDto::class.java)
        ).asJsonObject

        assertFalse(json.has("status"))
        assertFalse(json.has("statusIdeia"))
        assertFalse(json.has("decision"))
        assertFalse(json.has("approved"))
        assertFalse(json.has("projectId"))
    }

    private fun validResponse(): String {
        return """
            {
              "ideaId": 42,
              "strategicAlignment": 9,
              "impact": 8,
              "feasibility": 7,
              "innovation": 8,
              "overallScore": 8.1,
              "summary": "Resumo validado."
            }
        """.trimIndent()
    }
}
