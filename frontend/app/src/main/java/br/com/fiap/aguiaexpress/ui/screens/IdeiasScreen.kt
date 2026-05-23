package br.com.fiap.aguiaexpress.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Ideia
import br.com.fiap.aguiaexpress.domain.model.StatusIdeia
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.components.StatusTag
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.IdeiaViewModel

@Composable
fun IdeiasScreen(
    onCriarIdeiaClick: () -> Unit,
    viewModel: IdeiaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(message = "Carregando ideias...")
                }

                uiState.errorMessage != null -> {
                    ErrorMessage(
                        message = uiState.errorMessage ?: "Erro ao carregar ideias",
                        onRetry = {
                            viewModel.carregarIdeias()
                        }
                    )
                }

                uiState.ideias.isEmpty() -> {
                    EmptyState(message = "Nenhuma ideia encontrada.")
                }

                else -> {
                    IdeiaList(
                        ideias = uiState.ideias,
                        quantidadeVotos = uiState.votosPorIdeia,
                        ideiasVotadasPeloUsuario = uiState.ideiasVotadasPeloUsuario,
                        onAlternarVotoClick = { ideiaId ->
                            viewModel.alternarVotoIdeia(ideiaId)
                        },
                        onAtualizarStatus = { ideiaId, status ->
                            viewModel.atualizarStatusIdeia(
                                ideiaId = ideiaId,
                                status = status
                            )
                        },
                        onEditarClick = { ideiaId ->
                            // TODO: navegar para tela de edição
                        },
                        onExcluirClick = { ideiaId ->
                            // TODO: chamar função de deletar ideia
                        }
                    )
                }
            }
        }
    }



@Composable
private fun IdeiaList(
    ideias: List<Ideia>,
    quantidadeVotos: Map<Long, Int>,
    ideiasVotadasPeloUsuario: Set<Long>,
    onAlternarVotoClick: (Long) -> Unit,
    onAtualizarStatus: (Long, StatusIdeia) -> Unit,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ideias) { ideia ->
            IdeiaCard(
                ideia = ideia,
                quantidadeVotos = quantidadeVotos[ideia.id] ?: 0,
                usuarioJaVotou = ideiasVotadasPeloUsuario.contains(ideia.id),
                onAlternarVotoClick = onAlternarVotoClick,
                onAtualizarStatus = onAtualizarStatus,
                onEditarClick = onEditarClick,
                onExcluirClick = onExcluirClick
            )
        }
    }
}

@Composable
private fun IdeiaCard(
    ideia: Ideia,
    quantidadeVotos: Int,
    usuarioJaVotou: Boolean,
    onAlternarVotoClick: (Long) -> Unit,
    onAtualizarStatus: (Long, StatusIdeia) -> Unit,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            var menuExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ideia.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusTag(text = formatarStatusIdeia(ideia.status))

                    IconButton(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Editar")
                            },
                            onClick = {
                                menuExpanded = false
                                onEditarClick(ideia.id)
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Excluir")
                            },
                            onClick = {
                                menuExpanded = false
                                onExcluirClick(ideia.id)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ideia.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoText(
                label = "Problema",
                value = ideia.problema
            )

            InfoText(
                label = "Solução proposta",
                value = ideia.solucaoProposta
            )

            InfoText(
                label = "Categoria",
                value = ideia.nomeCategoria
            )

            InfoText(
                label = "Orientação",
                value = ideia.tituloOrientacao
            )

            InfoText(
                label = "Autor",
                value = ideia.nomeUsuario
            )


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onAlternarVotoClick(ideia.id)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = if (usuarioJaVotou) "Remover voto" else "Votar",
                            tint = if (usuarioJaVotou) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = quantidadeVotos.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onAtualizarStatus(ideia.id, StatusIdeia.PRIORIZADA)
                        }
                    ) {
                        Text("Priorizar")
                    }

                    Button(
                        onClick = {
                            onAtualizarStatus(ideia.id, StatusIdeia.APROVADA)
                        }
                    ) {
                        Text("Aprovar")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoText(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

private fun formatarStatusIdeia(status: String): String {
    return when (status) {
        "NOVA" -> "Nova"
        "EM_ANALISE" -> "Em análise"
        "PRIORIZADA" -> "Priorizada"
        "APROVADA" -> "Aprovada"
        "REJEITADA" -> "Rejeitada"
        "CONVERTIDA_EM_PROJETO" -> "Convertida em projeto"
        else -> status
    }
}

@Preview(showBackground = true)
@Composable
private fun IdeiasScreenPreview() {
    AguiaExpressTheme {
        IdeiasScreen(
            onCriarIdeiaClick = {}

        )
    }
}