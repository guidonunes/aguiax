package br.com.fiap.aguiaexpress.ui.screens.medalhas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Badge
import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.components.SemanticTag
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.ui.theme.SemanticTone
import br.com.fiap.aguiaexpress.viewmodel.BadgeUiState
import br.com.fiap.aguiaexpress.viewmodel.BadgeViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MedalhasScreen(viewModel: BadgeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.carregarMedalhas() }
    MedalhasPage(state, viewModel::carregarMedalhas)
}

@Composable
internal fun MedalhasPage(state: BadgeUiState, onRetry: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Medalhas", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() })
                Text("Acompanhe suas conquistas e desbloqueie novos objetivos dentro do app.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when {
            state.isLoading -> item { LoadingIndicator(message = "Carregando medalhas...") }
            state.errorMessage != null -> item { ErrorMessage(state.errorMessage, onRetry) }
            else -> {
                item(key = "progress") { AchievementSummaryCard(state.totalBadgesAtivasConquistadas, state.totalBadgesAtivas) }
                if (state.usuarioBadges.isEmpty() && state.badgesDisponiveis.isEmpty()) {
                    item { EmptyState(message = "Nenhuma medalha encontrada.") }
                } else {
                    item(key = "earned-title") { MedalhasSectionTitle("Minhas conquistas", "Medalhas que você já desbloqueou participando do app.") }
                    if (state.usuarioBadges.isEmpty()) item { EmptyState(message = "Você ainda não conquistou medalhas.") }
                    items(state.usuarioBadges, key = { "earned-${it.id}" }) { badge ->
                        MedalCard(badge.nomeBadge, badge.descricaoBadge, badge.criterioBadge, true, badge.dataConquista)
                    }
                    item(key = "available-title") { MedalhasSectionTitle("Medalhas disponíveis", "Objetivos que você ainda pode conquistar.") }
                    if (state.badgesDisponiveis.isEmpty()) item { EmptyState(message = "Nenhuma medalha disponível.") }
                    items(state.badgesDisponiveis, key = { "available-${it.id}" }) { badge ->
                        MedalCard(badge.nome, badge.descricao, badge.criterio, false)
                    }
                }
            }
        }
    }
}

internal fun achievementProgress(earned: Int, total: Int): Float =
    if (total <= 0) 0f else (earned.toFloat() / total).coerceIn(0f, 1f)

@Composable
internal fun AchievementSummaryCard(conquistadas: Int, totalAtivas: Int) {
    val scheme = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(28.dp))
                Text("Progresso de conquistas", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            }
            Text(if (totalAtivas > 0) "Você conquistou $conquistadas de $totalAtivas medalhas ativas."
                else "Nenhuma medalha ativa no catálogo.", style = MaterialTheme.typography.bodyMedium)
            if (totalAtivas > 0) LinearProgressIndicator(progress = { achievementProgress(conquistadas, totalAtivas) },
                modifier = Modifier.fillMaxWidth(), color = scheme.onTertiaryContainer,
                trackColor = scheme.surfaceContainerHigh)
        }
    }
}

@Composable
private fun MedalhasSectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun MedalCard(name: String, description: String, criterion: String, earned: Boolean, earnedAt: String? = null) {
    val scheme = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, if (earned) scheme.tertiary else scheme.outline),
        colors = CardDefaults.cardColors(containerColor = scheme.surface, contentColor = scheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (earned) 2.dp else 0.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = if (earned) scheme.tertiaryContainer else scheme.tertiaryContainer.copy(alpha = 0.35f),
                    contentColor = if (earned) scheme.onTertiaryContainer else scheme.onSurfaceVariant) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(badgeIcon(name), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(formatBadgeName(name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SemanticTag(if (earned) "Conquistada" else "Disponível",
                        if (earned) SemanticTone.ACHIEVEMENT else SemanticTone.NEUTRAL)
                }
            }
            MedalInfo("Descrição", description)
            MedalInfo("Critério", criterion)
            if (earned && earnedAt != null) MedalInfo("Conquistada em", formatBadgeDate(earnedAt))
        }
    }
}

@Composable
private fun MedalInfo(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun badgeIcon(name: String): ImageVector = when (name.uppercase(Locale.ROOT)) {
    "PRIMEIRA_IDEIA", "PRIMEIRA_IDEA" -> Icons.Default.Lightbulb
    "IDEIA_APROVADA" -> Icons.Default.CheckCircle
    else -> Icons.Default.EmojiEvents
}

private fun formatBadgeName(name: String): String = when (name.uppercase(Locale.ROOT)) {
    "PRIMEIRA_IDEIA", "PRIMEIRA_IDEA" -> "Primeira ideia"
    "IDEIA_APROVADA" -> "Ideia aprovada"
    "TOP_COLABORADOR" -> "Top colaborador"
    else -> name.lowercase().split('_').joinToString(" ") { it.replaceFirstChar { char -> char.titlecase() } }
}

private fun formatBadgeDate(value: String): String = runCatching {
    LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))
}.getOrDefault(value)

private fun previewMedals() = BadgeUiState(totalBadgesAtivas = 3, totalBadgesAtivasConquistadas = 1,
    usuarioBadges = listOf(UsuarioBadge(1, 7, "Pessoa", "sample@example.test", 1, "PRIMEIRA_IDEIA", "Sua primeira contribuição.",
        "Envie uma ideia.", "", "2026-09-15T12:00:00")),
    badgesDisponiveis = listOf(Badge(2, "IDEIA_APROVADA", "Uma ideia reconhecida pelo seu impacto.", "Tenha uma ideia aprovada.", "", 1)))

@Preview(showBackground = true)
@Composable
private fun MedalhasLightPreview() { AguiaExpressTheme(darkTheme = false) { MedalhasPage(previewMedals(), {}) } }

@Preview(showBackground = true)
@Composable
private fun MedalhasDarkPreview() { AguiaExpressTheme(darkTheme = true) { MedalhasPage(previewMedals(), {}) } }
