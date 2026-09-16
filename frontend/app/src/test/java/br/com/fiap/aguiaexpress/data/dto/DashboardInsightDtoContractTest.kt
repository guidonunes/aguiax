package br.com.fiap.aguiaexpress.data.dto

import br.com.fiap.aguiaexpress.domain.model.DashboardInsightScope
import com.google.gson.Gson
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DashboardInsightDtoContractTest {

    private val gson = Gson()

    @Test
    fun `maps every structured field and the exact trusted metrics snapshot`() {
        val insight = gson.fromJson(
            validResponse(),
            DashboardInsightDto::class.java
        ).toDomain()

        assertEquals(DashboardInsightScope.GLOBAL, insight.scope)
        assertEquals(null, insight.orientationId)
        assertEquals(4L, insight.metricsSnapshot.totalProjects)
        assertEquals(
            BigDecimal("850000.10"),
            insight.metricsSnapshot.totalInvestment
        )
        assertEquals(
            "O portfólio apresenta resultado positivo.",
            insight.executiveSummary
        )
        assertEquals(listOf("O retorno supera o investimento."), insight.highlights)
        assertEquals(emptyList<String>(), insight.attentionPoints)
        assertEquals(
            listOf("Acompanhar os projetos em andamento."),
            insight.recommendations
        )
        assertEquals(
            Instant.parse("2026-09-16T15:00:00Z"),
            insight.generatedAt
        )
    }

    @Test
    fun `validates global and orientation scope invariants`() {
        val invalidResponses = listOf(
            validResponse().replace(
                "\"orientationId\": null",
                "\"orientationId\": 42"
            ),
            validResponse()
                .replace("\"scope\": \"GLOBAL\"", "\"scope\": \"ORIENTATION\"")
                .replace("\"orientationId\": null", "\"orientationId\": 0")
        )

        invalidResponses.forEach { json ->
            assertThrows(IllegalArgumentException::class.java) {
                gson.fromJson(json, DashboardInsightDto::class.java).toDomain()
            }
        }
    }

    @Test
    fun `rejects missing blank or oversized structured text`() {
        val oversizedText = "a".repeat(801)
        val invalidResponses = listOf(
            validResponse().replace(
                "O portfólio apresenta resultado positivo.",
                "   "
            ),
            validResponse().replace(
                "O portfólio apresenta resultado positivo.",
                oversizedText
            ),
            validResponse().replace(
                "O retorno supera o investimento.",
                "   "
            )
        )

        invalidResponses.forEach { json ->
            assertThrows(IllegalArgumentException::class.java) {
                gson.fromJson(json, DashboardInsightDto::class.java).toDomain()
            }
        }
    }

    @Test
    fun `rejects invalid list bounds and malformed generation timestamp`() {
        val invalidResponses = listOf(
            validResponse().replace(
                "\"highlights\": [\"O retorno supera o investimento.\"]",
                "\"highlights\": []"
            ),
            validResponse().replace(
                "\"recommendations\": [\"Acompanhar os projetos em andamento.\"]",
                "\"recommendations\": []"
            ),
            validResponse().replace(
                "2026-09-16T15:00:00Z",
                "data-inválida"
            )
        )

        invalidResponses.forEach { json ->
            assertThrows(IllegalArgumentException::class.java) {
                gson.fromJson(json, DashboardInsightDto::class.java).toDomain()
            }
        }
    }

    @Test
    fun `rejects an absent or empty dashboard snapshot`() {
        val invalidResponses = listOf(
            validResponse().replace(
                Regex("\"metricsSnapshot\": \\{.*?\\},\\s*\"executiveSummary\"", RegexOption.DOT_MATCHES_ALL),
                "\"metricsSnapshot\": null, \"executiveSummary\""
            ),
            validResponse()
                .replace("\"totalProjects\": 4", "\"totalProjects\": 0")
                .replace("\"PLANEJADO\": 1", "\"PLANEJADO\": 0")
                .replace("\"EM_ANDAMENTO\": 2", "\"EM_ANDAMENTO\": 0")
                .replace("\"CONCLUIDO\": 1", "\"CONCLUIDO\": 0")
        )

        invalidResponses.forEach { json ->
            assertThrows(IllegalArgumentException::class.java) {
                gson.fromJson(json, DashboardInsightDto::class.java).toDomain()
            }
        }
    }

    private fun validResponse(): String {
        return """
            {
              "scope": "GLOBAL",
              "orientationId": null,
              "metricsSnapshot": {
                "totalProjects": 4,
                "projectsByStatus": {
                  "PLANEJADO": 1,
                  "EM_ANDAMENTO": 2,
                  "CONCLUIDO": 1,
                  "CANCELADO": 0
                },
                "totalInvestment": 850000.10,
                "totalFinancialReturn": 1200000.20,
                "totalCostReduction": 210000.30,
                "estimatedResult": 560000.40,
                "averageRoi": 32.75,
                "averageProductivityGain": 11.45
              },
              "executiveSummary": "O portfólio apresenta resultado positivo.",
              "highlights": ["O retorno supera o investimento."],
              "attentionPoints": [],
              "recommendations": ["Acompanhar os projetos em andamento."],
              "generatedAt": "2026-09-16T15:00:00Z"
            }
        """.trimIndent()
    }
}
