package br.com.fiap.aguiaexpress.ui.screens.medalhas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Badge
import br.com.fiap.aguiaexpress.domain.model.UsuarioBadge
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.BadgeViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MedalhasScreen(
    viewModel: BadgeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Medalhas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Acompanhe suas conquistas e desbloqueie novos objetivos dentro do app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        AchievementSummaryCard(
            conquistadas = uiState.totalBadgesAtivasConquistadas,
            totalAtivas = uiState.totalBadgesAtivas
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando medalhas...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar medalhas",
                    onRetry = {
                        viewModel.carregarMedalhas()
                    }
                )
            }

            uiState.badgesDisponiveis.isEmpty() && uiState.usuarioBadges.isEmpty() -> {
                EmptyState(message = "Nenhuma medalha encontrada.")
            }

            else -> {
                MedalhasContent(
                    usuarioBadges = uiState.usuarioBadges,
                    badgesDisponiveis = uiState.badgesDisponiveis
                )
            }
        }
    }
}

@Composable
private fun MedalhasContent(
    usuarioBadges: List<UsuarioBadge>,
    badgesDisponiveis: List<Badge>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MedalhasSectionTitle(
                title = "Minhas conquistas",
                subtitle = "Medalhas que você já desbloqueou participando do app."
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (usuarioBadges.isEmpty()) {
            item {
                Text(
                    text = "Você ainda não conquistou medalhas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(usuarioBadges) { usuarioBadge ->
                UsuarioBadgeCard(usuarioBadge = usuarioBadge)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            MedalhasSectionTitle(
                title = "Medalhas disponíveis",
                subtitle = "Objetivos que você ainda pode conquistar."
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (badgesDisponiveis.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma medalha disponível.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(badgesDisponiveis) { badge ->
                BadgeCard(badge = badge)
            }
        }
    }
}

@Composable
private fun UsuarioBadgeCard(
    usuarioBadge: UsuarioBadge
) {
    BadgeCard(
        nomeBadge = usuarioBadge.nomeBadge,
        descricao = usuarioBadge.descricaoBadge,
        criterio = usuarioBadge.criterioBadge,
        dataConquista = usuarioBadge.dataConquista,
        isConquistada = true
    )
}

private data class BadgeColors(
    val containerColor: Color,
    val titleColor: Color,
    val accentColor: Color
)

@Composable
private fun BadgeCard(
    badge: Badge
) {
    BadgeCard(
        nomeBadge = badge.nome,
        descricao = badge.descricao,
        criterio = badge.criterio,
        isConquistada = false
    )
}

@Composable
private fun AchievementSummaryCard(
    conquistadas: Int,
    totalAtivas: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Progresso de conquistas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Você conquistou $conquistadas de $totalAtivas medalhas ativas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MedalhasSectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BadgeCard(
    nomeBadge: String,
    descricao: String,
    criterio: String,
    isConquistada: Boolean,
    dataConquista: String? = null
) {
    val badgeColors = getBadgeColors(
        nomeBadge = nomeBadge,
        isConquistada = isConquistada
    )

    val badgeIcon = getBadgeIcon(nomeBadge)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isConquistada) {
                    Modifier.border(
                        width = 1.dp,
                        color = badgeColors.accentColor.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(24.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = badgeColors.containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isConquistada) 6.dp else 1.dp
        )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(badgeColors.accentColor)
            )

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isConquistada) {
                                        badgeColors.accentColor.copy(alpha = 0.22f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(
                                    if (isConquistada) 10.dp else 8.dp
                                )
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = nomeBadge,
                                tint = if (isConquistada) {
                                    badgeColors.accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                },
                                modifier = Modifier.size(
                                    if (isConquistada) 30.dp else 24.dp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = formatarNomeBadge(nomeBadge),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isConquistada) {
                                    FontWeight.ExtraBold
                                } else {
                                    FontWeight.SemiBold
                                }
                            ),
                            color = if (isConquistada) {
                                badgeColors.titleColor
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    BadgeStatusTag(
                        isConquistada = isConquistada
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(18.dp)
        ) {


            InfoText(
                label = "Descrição",
                value = descricao
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoText(
                label = "Critério",
                value = criterio
            )

            if (dataConquista != null) {
                Spacer(modifier = Modifier.height(8.dp))

                InfoText(
                    label = "Conquistada em",
                    value = formatarDataConquista(dataConquista)
                )
            }
        }
    }
}

@Composable
private fun InfoText(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("$label: ")
            }

            append(value)
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
        modifier = modifier
    )
}

@Composable
private fun getBadgeColors(
    nomeBadge: String,
    isConquistada: Boolean
): BadgeColors {
    val normalizedName = nomeBadge.uppercase()

    if (!isConquistada) {
        return BadgeColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            accentColor = MaterialTheme.colorScheme.outline
        )
    }

    return when (normalizedName) {
        "PRIMEIRA_IDEIA", "PRIMEIRA_IDEA" -> BadgeColors(
            containerColor = Color(0xFFFFF3CD),
            titleColor = Color(0xFF8A5A00),
            accentColor = Color(0xFFFFB300)
        )

        "IDEIA_APROVADA" -> BadgeColors(
            containerColor = Color(0xFFE8F5E9),
            titleColor = Color(0xFF2E7D32),
            accentColor = Color(0xFF43A047)
        )

        "TOP_COLABORADOR" -> BadgeColors(
            containerColor = Color(0xFFEDE7F6),
            titleColor = Color(0xFF5E35B1),
            accentColor = Color(0xFF7E57C2)
        )

        else -> BadgeColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            titleColor = MaterialTheme.colorScheme.primary,
            accentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun BadgeStatusTag(
    isConquistada: Boolean
) {
    val backgroundColor = if (isConquistada) {
        Color(0xFFE8F5E9)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isConquistada) {
        Color(0xFF2E7D32)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val text = if (isConquistada) {
        "Conquistada"
    } else {
        "Disponível"
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

private fun getBadgeIcon(nomeBadge: String): ImageVector {
    return when (nomeBadge.uppercase()) {
        "PRIMEIRA_IDEIA", "PRIMEIRA_IDEA" -> Icons.Default.Lightbulb
        "IDEIA_APROVADA" -> Icons.Default.CheckCircle
        "TOP_COLABORADOR" -> Icons.Default.EmojiEvents
        else -> Icons.Default.EmojiEvents
    }
}

private fun formatarNomeBadge(nomeBadge: String): String {
    return when (nomeBadge.uppercase()) {
        "PRIMEIRA_IDEIA", "PRIMEIRA_IDEA" -> "Primeira ideia"
        "IDEIA_APROVADA" -> "Ideia aprovada"
        "TOP_COLABORADOR" -> "Top colaborador"
        else -> nomeBadge
            .lowercase()
            .split("_")
            .joinToString(" ") { palavra ->
                palavra.replaceFirstChar { char ->
                    char.uppercase()
                }
            }
    }
}

private fun formatarDataConquista(data: String): String {
    return try {
        val partes = data.split("T")

        if (partes.size < 2) {
            return data
        }

        val dataPartes = partes[0].split("-")
        val horaPartes = partes[1].split(":")

        if (dataPartes.size < 3 || horaPartes.size < 2) {
            return data
        }

        val ano = dataPartes[0]
        val mes = dataPartes[1]
        val dia = dataPartes[2]

        val hora = horaPartes[0]
        val minuto = horaPartes[1]

        "$dia/$mes/$ano às $hora:$minuto"
    } catch (e: Exception) {
        data
    }
}

@Preview(showBackground = true)
@Composable
private fun MedalhasScreenPreview() {
    AguiaExpressTheme {
        MedalhasScreen()
    }
}
