package br.com.fiap.aguiax.ui.screens.medalhas

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.fiap.aguiax.core.session.SessionManager
import br.com.fiap.aguiax.data.dto.BadgeDto
import br.com.fiap.aguiax.data.dto.UsuarioBadgeDto
import br.com.fiap.aguiax.data.remote.api.BadgeApi
import br.com.fiap.aguiax.data.remote.api.UsuarioBadgeApi
import br.com.fiap.aguiax.data.repository.BadgeRepository
import br.com.fiap.aguiax.data.repository.UsuarioBadgeRepository
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import br.com.fiap.aguiax.viewmodel.BadgeViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MedalhasRefreshTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private lateinit var owner: ScreenOwner
    private lateinit var model: BadgeViewModel
    private val oldSession = listOf(SessionManager.token, SessionManager.usuarioId?.toString(),
        SessionManager.nome, SessionManager.email, SessionManager.perfil)

    @After fun cleanUp() {
        compose.runOnIdle {
            store.clear()
            if (oldSession.all { it != null }) SessionManager.salvarSessao(
                oldSession[0]!!, oldSession[1]!!.toLong(), oldSession[2]!!, oldSession[3]!!, oldSession[4]!!)
            else SessionManager.limparSessao()
        }
    }

    @Test fun returningToMedalsFetchesNewAwardsAndRemovesThemFromAvailable() {
        var awards = emptyList<UsuarioBadgeDto>()
        var calls = 0
        showScreen(object : UsuarioBadgeApi {
            override suspend fun listarBadgesPorUsuario(usuarioId: Long): List<UsuarioBadgeDto> {
                assertEquals(42L, usuarioId)
                calls++
                return awards
            }
        })
        compose.onNodeWithText("Você conquistou 0 de 1 medalhas ativas.").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, calls)
            owner.registry.currentState = Lifecycle.State.STARTED
            awards = listOf(earnedBadge())
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.onNodeWithText("Você conquistou 1 de 1 medalhas ativas.").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(2, calls)
            assertEquals(1, model.uiState.value.usuarioBadges.size)
            assertEquals(0, model.uiState.value.badgesDisponiveis.size)
        }
    }

    @Test fun aCancelledOlderRefreshCannotReplaceTheNewAwards() {
        val firstResponse = CompletableDeferred<List<UsuarioBadgeDto>>()
        var calls = 0
        showScreen(object : UsuarioBadgeApi {
            override suspend fun listarBadgesPorUsuario(usuarioId: Long): List<UsuarioBadgeDto> {
                calls++
                return if (calls == 1) withContext(NonCancellable) { firstResponse.await() }
                    else listOf(earnedBadge())
            }
        })
        compose.runOnIdle {
            assertEquals(1, calls)
            owner.registry.currentState = Lifecycle.State.STARTED
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.onNodeWithText("Você conquistou 1 de 1 medalhas ativas.").assertIsDisplayed()
        compose.runOnIdle { firstResponse.complete(emptyList()) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(2, calls)
            assertEquals(1, model.uiState.value.usuarioBadges.size)
            assertEquals(1, model.uiState.value.totalBadgesAtivasConquistadas)
        }
    }

    private fun showScreen(api: UsuarioBadgeApi) {
        compose.runOnIdle {
            SessionManager.salvarSessao("test-only-token", 42L, "Ana", "ana@test.invalid", "OPERACIONAL")
            owner = ScreenOwner()
            owner.registry.currentState = Lifecycle.State.RESUMED
            val catalog = object : BadgeApi {
                override suspend fun listarBadges() = listOf(badge())
                override suspend fun listarBadgesAtivas() = listOf(badge())
                override suspend fun buscarBadgePorId(id: Long) = badge()
            }
            model = BadgeViewModel(BadgeRepository(catalog), UsuarioBadgeRepository(api))
            store.put("medals", model)
        }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                AguiaXTheme { MedalhasScreen(model) }
            }
        }
    }

    private class ScreenOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }

    private fun badge() = BadgeDto(1L, "PRIMEIRA_IDEIA", "Primeira contribuição", "Cadastrar uma ideia", "primeira_ideia", 1)
    private fun earnedBadge() = UsuarioBadgeDto(7L, 42L, "Ana", "ana@test.invalid", 1L,
        "PRIMEIRA_IDEIA", "Primeira contribuição", "Cadastrar uma ideia", "primeira_ideia", "2026-09-15T12:00:00")
}
