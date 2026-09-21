package br.com.fiap.aguiax.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AvaliacaoIdeiaValidationTest {

    @Test
    fun `accepts both inclusive score boundaries`() {
        assertEquals(
            PontuacoesAvaliacaoIdeia(impacto = 0, viabilidade = 10),
            validarPontuacoesAvaliacaoIdeia("0", "10")
        )
        assertEquals(
            PontuacoesAvaliacaoIdeia(impacto = 10, viabilidade = 0),
            validarPontuacoesAvaliacaoIdeia("10", "0")
        )
    }

    @Test
    fun `accepts valid intermediate scores`() {
        assertEquals(
            PontuacoesAvaliacaoIdeia(impacto = 8, viabilidade = 7),
            validarPontuacoesAvaliacaoIdeia("8", "7")
        )
    }

    @Test
    fun `rejects blank non numeric and out of range scores`() {
        listOf(
            "" to "7",
            "8" to "",
            "impacto" to "7",
            "8" to "viabilidade",
            "-1" to "7",
            "8" to "11"
        ).forEach { (impacto, viabilidade) ->
            assertNull(
                validarPontuacoesAvaliacaoIdeia(
                    impacto = impacto,
                    viabilidade = viabilidade
                )
            )
        }
    }
}
