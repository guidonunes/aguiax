package br.com.fiap.aguiaexpress.data.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class IdeiaDtoContractTest {

    private val gson = Gson()

    @Test
    fun `maps every backend idea status without falling back to NOVA`() {
        val backendStatuses = listOf(
            "NOVA",
            "EM_ANALISE",
            "PRIORIZADA",
            "APROVADA",
            "REJEITADA",
            "CONVERTIDA_EM_PROJETO"
        )

        backendStatuses.forEach { backendStatus ->
            val dto = gson.fromJson(
                ideaJson(statusField = "\"statusIdeia\": \"$backendStatus\","),
                IdeiaDto::class.java
            )

            assertEquals(backendStatus, dto.statusIdeia)
            assertEquals(backendStatus, dto.toDomain().status)
        }
    }

    @Test
    fun `rejects an idea response without statusIdeia`() {
        val dto = gson.fromJson(
            ideaJson(statusField = ""),
            IdeiaDto::class.java
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            dto.toDomain()
        }

        assertEquals(
            "A resposta da ideia não contém o campo obrigatório statusIdeia.",
            exception.message
        )
    }

    private fun ideaJson(statusField: String): String {
        return """
            {
              "id": 42,
              "usuarioId": 7,
              "categoriaId": 3,
              "orientacaoId": 4,
              "titulo": "Ideia",
              "descricao": "Descrição",
              "problema": "Problema",
              "solucaoProposta": "Solução",
              $statusField
              "nomeUsuario": "Usuário",
              "nomeCategoria": "Tecnologia",
              "tituloOrientacao": "Orientação"
            }
        """.trimIndent()
    }
}
