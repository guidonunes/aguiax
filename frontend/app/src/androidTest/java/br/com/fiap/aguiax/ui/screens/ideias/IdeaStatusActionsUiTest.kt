package br.com.fiap.aguiax.ui.screens.ideias

import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.fiap.aguiax.domain.model.StatusIdeia
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IdeaStatusActionsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun newIdeaOffersOnlyStartAnalysis() {
        compose.setContent { AguiaXTheme { Surface { IdeaStatusActions("NOVA") {} } } }
        compose.onNodeWithText("Iniciar análise").assertIsDisplayed()
        compose.onNodeWithText("Priorizar").assertDoesNotExist()
        compose.onNodeWithText("Aprovar").assertDoesNotExist()
    }

    @Test fun managerCanStartAnalysisPrioritizeAndApproveWithoutInvalidActions() {
        val status = mutableStateOf("NOVA")
        val requests = mutableListOf<StatusIdeia>()
        compose.setContent { AguiaXTheme { Surface {
            IdeaStatusActions(status.value) { target -> requests += target; status.value = target.name }
        } } }
        compose.onNodeWithText("Iniciar análise").performClick()
        compose.onNodeWithText("Priorizar").performClick()
        compose.onNodeWithText("Priorizar").assertDoesNotExist()
        compose.onNodeWithText("Aprovar").performClick()
        compose.onNodeWithText("Aprovar").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(listOf(StatusIdeia.EM_ANALISE, StatusIdeia.PRIORIZADA, StatusIdeia.APROVADA), requests)
            assertEquals("APROVADA", status.value)
        }
    }

    @Test fun managerCanApproveAnIdeaAlreadyUnderAnalysis() {
        var requested: StatusIdeia? = null
        compose.setContent { AguiaXTheme { Surface {
            IdeaStatusActions("EM_ANALISE") { requested = it }
        } } }
        compose.onNodeWithText("Aprovar").performClick()
        compose.runOnIdle { assertEquals(StatusIdeia.APROVADA, requested) }
    }

    @Test fun terminalAndUnknownStatusesDoNotOfferManagerTransitions() {
        val status = mutableStateOf("APROVADA")
        compose.setContent { AguiaXTheme { Surface { IdeaStatusActions(status.value) {} } } }
        listOf("APROVADA", "REJEITADA", "CONVERTIDA_EM_PROJETO", "UNKNOWN").forEach {
            compose.runOnIdle { status.value = it }
            compose.onNodeWithText("Aprovar").assertDoesNotExist()
            compose.onNodeWithText("Priorizar").assertDoesNotExist()
            compose.onNodeWithText("Iniciar análise").assertDoesNotExist()
        }
    }

    @Test fun pendingRequestDisablesActionsUntilItFinishes() {
        val updating = mutableStateOf(true)
        compose.setContent { AguiaXTheme { Surface {
            IdeaStatusActions("EM_ANALISE", isUpdating = updating.value) {}
        } } }
        compose.onNodeWithText("Aprovar").assertIsNotEnabled()
        compose.onNodeWithText("Priorizar").assertIsNotEnabled()
        compose.runOnIdle { updating.value = false }
        compose.onNodeWithText("Aprovar").assertIsEnabled()
        compose.onNodeWithText("Priorizar").assertIsEnabled()
    }
}
