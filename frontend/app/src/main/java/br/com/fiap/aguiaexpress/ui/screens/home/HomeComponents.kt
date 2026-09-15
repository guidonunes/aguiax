package br.com.fiap.aguiaexpress.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import br.com.fiap.aguiaexpress.ui.components.AguiaTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.ui.components.StatusTag
import br.com.fiap.aguiaexpress.ui.theme.SemanticTone
import br.com.fiap.aguiaexpress.ui.theme.toneColors
import br.com.fiap.aguiaexpress.ui.theme.semanticColors

internal fun HomeSymbol.tone(): SemanticTone = when (this) {
    HomeSymbol.IDEA, HomeSymbol.PROJECT, HomeSymbol.INVESTMENT, HomeSymbol.RETURN, HomeSymbol.ROI, HomeSymbol.STRATEGY -> SemanticTone.INFO
    HomeSymbol.BADGE -> SemanticTone.ACHIEVEMENT
    HomeSymbol.VOTE -> SemanticTone.PRIMARY
    HomeSymbol.APPROVED -> SemanticTone.SUCCESS
    HomeSymbol.REVIEW -> SemanticTone.WARNING
}

private fun HomeSymbol.icon(): ImageVector = when (this) {
    HomeSymbol.IDEA -> Icons.Default.Lightbulb
    HomeSymbol.BADGE -> Icons.Default.EmojiEvents
    HomeSymbol.VOTE -> Icons.Default.HowToVote
    HomeSymbol.APPROVED -> Icons.Default.CheckCircle
    HomeSymbol.REVIEW -> Icons.Default.RateReview
    HomeSymbol.PROJECT -> Icons.Default.Work
    HomeSymbol.INVESTMENT -> Icons.Default.AccountBalanceWallet
    HomeSymbol.RETURN, HomeSymbol.ROI -> Icons.AutoMirrored.Filled.TrendingUp
    HomeSymbol.STRATEGY -> Icons.Default.Flag
}

@Composable
internal fun HomeHeader(greeting: String, role: ApplicationRole?, isLoading: Boolean, onRefresh: () -> Unit) {
    val roleLabel = when (role) {
        ApplicationRole.OPERACIONAL -> "SUA CONTRIBUIÇÃO"
        ApplicationRole.GESTOR -> "GESTÃO DA INOVAÇÃO"
        ApplicationRole.LIDER -> "VISÃO EXECUTIVA"
        null -> "INOVAÇÃO EM MOVIMENTO"
    }
    val subtitle = when (role) {
        ApplicationRole.OPERACIONAL -> "Veja como suas ideias estão evoluindo."
        ApplicationRole.GESTOR -> "Concentre-se nas decisões que fazem a inovação avançar."
        ApplicationRole.LIDER -> "Acompanhe o impacto das iniciativas estratégicas."
        null -> "Seu ponto de partida no AguiaExpress."
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(roleLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f))
            IconButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar resumo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(greeting.ifBlank { "Olá" }, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun HomeSection(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).semantics { heading() })
            if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
        }
        content()
    }
}

@Composable
internal fun HomeMetricGrid(metrics: List<HomeMetric>) {
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val density = LocalDensity.current
    val valueStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cardTextWidth = with(density) { ((maxWidth - 12.dp) / 2 - 32.dp).toPx() }
        val valuesFit = metrics.all { metric ->
            metric.value.split(' ').all { word ->
                textMeasurer.measure(androidx.compose.ui.text.AnnotatedString(word), style = valueStyle,
                    softWrap = false).size.width <= cardTextWidth
            }
        }
        val columns = if (maxWidth >= 280.dp && density.fontScale <= 1.35f && valuesFit) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { metric -> HomeMetricCard(metric, Modifier.weight(1f).fillMaxHeight()) }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeMetricCard(metric: HomeMetric, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp).semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeIcon(metric.symbol)
            Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.heightIn(min = 32.dp))
            Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            if (metric.hint != null) Text(metric.hint, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HomeIcon(symbol: HomeSymbol) {
    val colors = toneColors(symbol.tone())
    Surface(shape = CircleShape, color = colors.container, contentColor = colors.content) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(symbol.icon(), contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun HomeQuickAction(title: String, detail: String, symbol: HomeSymbol, onClick: () -> Unit, prominent: Boolean = false) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = if (prominent) MaterialTheme.semanticColors.primaryAction
            else MaterialTheme.colorScheme.surface,
            contentColor = if (prominent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (prominent) Icon(symbol.icon(), contentDescription = null, modifier = Modifier.size(24.dp))
            else HomeIcon(symbol)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun HomeStatusSummary(pipeline: List<HomeStatusCount>) {
    HomeListCard {
        pipeline.filter { it.status in setOf("NOVA", "EM_ANALISE", "PRIORIZADA", "APROVADA") || it.count > 0 }
            .forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp).semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusTag(homeStatusLabel(entry.status), modifier = Modifier.weight(1f, fill = false))
                    Text(entry.count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp))
                }
            }
    }
}

@Composable
internal fun HomeActivityList(activities: List<HomeActivity>, onNavigate: (HomeDestination) -> Unit) {
    HomeListCard {
        activities.forEachIndexed { index, activity ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            Surface(onClick = { onNavigate(activity.destination) }, color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeIcon(activity.symbol)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(activity.description, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activity.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(formatHomeTimestamp(activity.timestamp), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeAttentionList(items: List<HomeAttention>, onNavigate: (HomeDestination) -> Unit) {
    HomeListCard {
        items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            Surface(onClick = { onNavigate(item.destination) }, color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    HomeIcon(HomeSymbol.REVIEW)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
internal fun HomeStrategyList(strategies: List<HomeStrategy>) {
    HomeListCard {
        strategies.forEachIndexed { index, strategy ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            Row(Modifier.fillMaxWidth().padding(16.dp).semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(strategy.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(strategy.projectCount?.let { "$it ${if (it == 1L) "projeto" else "projetos"}" } ?: "Indisponível",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun HomeExecutiveHighlights(metrics: List<HomeMetric>) {
    HomeListCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.forEach { metric ->
                Column(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(metric.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun HomeAvailabilityNotice(warnings: List<String>, onRetry: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo parcialmente disponível", style = MaterialTheme.typography.titleSmall)
            warnings.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            TextButton(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}

@Composable
private fun HomeListCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), content = content)
}
