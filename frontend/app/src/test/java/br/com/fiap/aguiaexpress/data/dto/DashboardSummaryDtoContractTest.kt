package br.com.fiap.aguiaexpress.data.dto

import com.google.gson.Gson
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DashboardSummaryDtoContractTest {

    private val gson = Gson()

    @Test
    fun `maps every backend dashboard field without recalculating decimals`() {
        val summary = gson.fromJson(
            validResponse(),
            DashboardSummaryDto::class.java
        ).toDomain()

        assertEquals(4L, summary.totalProjects)
        assertEquals(2L, summary.projectsInProgress)
        assertEquals(BigDecimal("850000.10"), summary.totalInvestment)
        assertEquals(BigDecimal("1200000.20"), summary.totalFinancialReturn)
        assertEquals(BigDecimal("210000.30"), summary.totalCostReduction)
        assertEquals(BigDecimal("560000.40"), summary.estimatedResult)
        assertEquals(BigDecimal("32.75"), summary.averageRoi)
        assertEquals(BigDecimal("11.45"), summary.averageProductivityGain)
    }

    @Test
    fun `rejects a response missing a required project status`() {
        val dto = gson.fromJson(
            validResponse().replace("\"CANCELADO\": 0", "\"OUTRO\": 0"),
            DashboardSummaryDto::class.java
        )

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }
    }

    @Test
    fun `rejects negative or inconsistent project counts`() {
        val invalidResponses = listOf(
            validResponse().replace("\"EM_ANDAMENTO\": 2", "\"EM_ANDAMENTO\": -1"),
            validResponse().replace("\"totalProjects\": 4", "\"totalProjects\": 9")
        )

        invalidResponses.forEach { json ->
            val dto = gson.fromJson(json, DashboardSummaryDto::class.java)
            assertThrows(IllegalArgumentException::class.java) {
                dto.toDomain()
            }
        }
    }

    @Test
    fun `rejects a missing backend metric`() {
        val dto = gson.fromJson(
            validResponse().replace("\"averageRoi\": 32.75,", ""),
            DashboardSummaryDto::class.java
        )

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }
    }

    private fun validResponse(): String {
        return """
            {
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
            }
        """.trimIndent()
    }
}
