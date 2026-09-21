package br.com.fiap.aguiax.data.dto

import com.google.gson.Gson
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class IdeiaDtoContractTest {

    private val gson = Gson()

    @Test
    fun `maps actual vote totals and last update for home`() {
        val json = ideaJson(statusField = "\"statusIdeia\": \"APROVADA\",")
            .replace("\"id\": 42,", "\"id\": 42, \"totalVotos\": 24, \"dataAtualizacao\": \"2026-09-15T12:00:00\",")
        val idea = gson.fromJson(json, IdeiaDto::class.java).toDomain()
        assertEquals(24, idea.totalVotos)
        assertEquals("2026-09-15T12:00:00", idea.dataAtualizacao)
    }

    @Test
    fun `missing vote totals remain unavailable rather than invented zero`() {
        val idea = gson.fromJson(ideaJson("\"statusIdeia\": \"NOVA\","), IdeiaDto::class.java).toDomain()
        assertNull(idea.totalVotos)
        assertNull(idea.dataAtualizacao)
    }

    @Test
    fun `preserves creation time for newest first feed ordering`() {
        val dto = gson.fromJson(
            ideaJson(statusField = "\"statusIdeia\": \"NOVA\",")
                .replace("\"id\": 42,", "\"id\": 42, \"dataCriacao\": \"2026-09-15T12:00:00\","),
            IdeiaDto::class.java
        )
        assertEquals("2026-09-15T12:00:00", dto.toDomain().dataCriacao)
    }

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

    @Test
    fun `maps persisted manager scores and decimal priority`() {
        val dto = gson.fromJson(
            ideaJson(
                statusField = "\"statusIdeia\": \"EM_ANALISE\",",
                impacto = "8",
                viabilidade = "7",
                prioridade = "4.35"
            ),
            IdeiaDto::class.java
        )

        val ideia = dto.toDomain()

        assertEquals(8, ideia.impacto)
        assertEquals(7, ideia.viabilidade)
        assertEquals(BigDecimal("4.35"), ideia.prioridade)
    }

    @Test
    fun `keeps legacy missing manager scores nullable`() {
        val dto = gson.fromJson(
            ideaJson(
                statusField = "\"statusIdeia\": \"NOVA\",",
                impacto = "null",
                viabilidade = "null"
            ),
            IdeiaDto::class.java
        )

        val ideia = dto.toDomain()

        assertNull(ideia.impacto)
        assertNull(ideia.viabilidade)
    }

    private fun ideaJson(
        statusField: String,
        impacto: String = "8",
        viabilidade: String = "7",
        prioridade: String = "4.35"
    ): String {
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
              "impacto": $impacto,
              "viabilidade": $viabilidade,
              "prioridade": $prioridade,
              $statusField
              "nomeUsuario": "Usuário",
              "nomeCategoria": "Tecnologia",
              "tituloOrientacao": "Orientação"
            }
        """.trimIndent()
    }
}
