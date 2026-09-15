package br.com.fiap.aguiaexpress.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.fiap.aguiaexpress.ui.theme.SemanticTone
import java.util.Locale

internal fun statusTone(status: String): SemanticTone = when (status.uppercase(Locale.ROOT)) {
    "NOVA", "NOVO", "PLANEJADO" -> SemanticTone.INFO
    "EM ANÁLISE", "EM_ANALISE", "EM ANDAMENTO", "EM_ANDAMENTO" -> SemanticTone.WARNING
    "PRIORIZADA" -> SemanticTone.ACHIEVEMENT
    "APROVADA", "CONCLUÍDO", "CONCLUIDO", "CONVERTIDA EM PROJETO", "CONVERTIDA_EM_PROJETO" -> SemanticTone.SUCCESS
    "REJEITADA", "CANCELADO" -> SemanticTone.ERROR
    else -> SemanticTone.NEUTRAL
}

@Composable
fun StatusTag(text: String, modifier: Modifier = Modifier) { SemanticTag(text, statusTone(text), modifier) }
