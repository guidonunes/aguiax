package br.com.fiap.aguiaexpress.ui.screens.ideias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.domain.model.StatusIdeia
import br.com.fiap.aguiaexpress.ui.components.AguiaButton
import br.com.fiap.aguiaexpress.ui.components.AguiaOutlinedButton

// Availability of these UI actions only; the backend remains authoritative.
internal fun statusActionsForIdea(currentStatus: String): List<StatusIdeia> = when (currentStatus) {
    StatusIdeia.NOVA.name -> listOf(StatusIdeia.EM_ANALISE)
    StatusIdeia.EM_ANALISE.name -> listOf(StatusIdeia.PRIORIZADA, StatusIdeia.APROVADA)
    StatusIdeia.PRIORIZADA.name -> listOf(StatusIdeia.APROVADA)
    else -> emptyList()
}

@Composable
internal fun IdeaStatusActions(
    currentStatus: String,
    isUpdating: Boolean = false,
    onStatusSelected: (StatusIdeia) -> Unit
) {
    val actions = statusActionsForIdea(currentStatus)
    if (actions.isEmpty()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
        actions.forEach { target ->
            val label = when (target) {
                StatusIdeia.EM_ANALISE -> "Iniciar análise"
                StatusIdeia.PRIORIZADA -> "Priorizar"
                else -> "Aprovar"
            }
            if (target == StatusIdeia.APROVADA) {
                AguiaButton(onClick = { onStatusSelected(target) }, enabled = !isUpdating) { Text(label) }
            } else {
                AguiaOutlinedButton(onClick = { onStatusSelected(target) }, enabled = !isUpdating) { Text(label) }
            }
        }
    }
}
