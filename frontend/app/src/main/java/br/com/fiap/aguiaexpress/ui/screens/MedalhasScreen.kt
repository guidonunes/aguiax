package br.com.fiap.aguiaexpress.ui.screens.medalhas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

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

            uiState.badgesAtivas.isEmpty() && uiState.usuarioBadges.isEmpty() -> {
                EmptyState(message = "Nenhuma medalha encontrada.")
            }

            else -> {
                MedalhasContent(
                    usuarioBadges = uiState.usuarioBadges,
                    badgesAtivas = uiState.badgesAtivas
                )
            }
        }
    }
}

@Composable
private fun MedalhasContent(
    usuarioBadges: List<UsuarioBadge>,
    badgesAtivas: List<Badge>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Minhas conquistas",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
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

            Text(
                text = "Medalhas disponíveis",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (badgesAtivas.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma medalha disponível.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(badgesAtivas) { badge ->
                BadgeCard(badge = badge)
            }
        }
    }
}

@Composable
private fun UsuarioBadgeCard(
    usuarioBadge: UsuarioBadge
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = usuarioBadge.nomeBadge,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = usuarioBadge.descricaoBadge,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Critério: ${usuarioBadge.criterioBadge}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Conquistada em: ${usuarioBadge.dataConquista}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun BadgeCard(
    badge: Badge
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = badge.nome,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = badge.descricao,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Critério: ${badge.criterio}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun MedalhasScreenPreview() {
    AguiaExpressTheme {
        MedalhasScreen()
    }
}