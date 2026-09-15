package br.com.fiap.aguiaexpress.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import br.com.fiap.aguiaexpress.viewmodel.AvaliacaoIdeiaUiState
import br.com.fiap.aguiaexpress.viewmodel.AiAvaliacaoIdeiaUiState

@Composable
fun IdeiasScreen(
    podeEditarExcluirIdeia: Boolean,
    podeAtualizarStatusIdeia: Boolean,
    podeVotarEmIdeia: Boolean,
    podeAvaliarIdeia: Boolean,
    podeSolicitarAvaliacaoIa: Boolean,
    usuarioIdLogado: Long?,
    onCriarIdeiaClick: () -> Unit,
    onEditarIdeiaClick: (Long) -> Unit,
    viewModel: IdeiaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val avaliacaoUiState by
    viewModel.avaliacaoIdeiaUiState.collectAsState()
    val aiAvaliacaoUiState by
    viewModel.aiAvaliacaoIdeiaUiState.collectAsState()

    var ideiaIdParaExcluir by remember { mutableStateOf<Long?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.carregarIdeias()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Feed de Ideias",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acompanhe, vote e interaja com as ideias cadastradas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        votosUsuario = uiState.ideiasVotadasPeloUsuario,
                        podeVotarEmIdeia = podeVotarEmIdeia,
                        podeEditarExcluirIdeia = podeEditarExcluirIdeia,
                        podeAtualizarStatusIdeia = podeAtualizarStatusIdeia,
                        podeAvaliarIdeia = podeAvaliarIdeia,
                        podeSolicitarAvaliacaoIa = podeSolicitarAvaliacaoIa,
                        usuarioIdLogado = usuarioIdLogado,
                        onAlternarVotoClick = { ideiaId ->
                            viewModel.alternarVotoIdeia(ideiaId)
                        },
                        onAtualizarStatusClick = { ideiaId, status ->
                            viewModel.atualizarStatusIdeia(ideiaId, status)
                        },
                        onAvaliarIdeiaClick = { ideia ->
                            viewModel.abrirAvaliacaoIdeia(ideia)
                        },
                        onSolicitarAvaliacaoIaClick = { ideia ->
                            viewModel.solicitarAvaliacaoIa(ideia)
                        },
                        onEditarIdeiaClick = onEditarIdeiaClick,
                        onExcluirIdeiaClick = { ideiaId ->
                            ideiaIdParaExcluir = ideiaId
                        }
                    )

                }
            }
        }

    if (ideiaIdParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                ideiaIdParaExcluir = null
            },
            title = {
                Text(text = "Excluir ideia")
            },
            text = {
                Text(text = "Tem certeza que deseja excluir esta ideia? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ideiaIdParaExcluir?.let { ideiaId ->
                            viewModel.deletarIdeia(ideiaId)
                        }

                        ideiaIdParaExcluir = null
                    }
                ) {
                    Text(text = "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        ideiaIdParaExcluir = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (avaliacaoUiState.ideiaId != null) {
        AvaliacaoIdeiaDialog(
            state = avaliacaoUiState,
            onImpactoChange = viewModel::atualizarImpactoAvaliacao,
            onViabilidadeChange = viewModel::atualizarViabilidadeAvaliacao,
            onConfirm = viewModel::salvarAvaliacaoIdeia,
            onDismiss = viewModel::fecharAvaliacaoIdeia
        )
    }

    if (aiAvaliacaoUiState.ideiaId != null) {
        AiAvaliacaoIdeiaDialog(
            state = aiAvaliacaoUiState,
            onRetry = viewModel::repetirAvaliacaoIa,
            onDismiss = viewModel::fecharAvaliacaoIa
        )
    }
}

@Composable
private fun IdeiaList(
    ideias: List<Ideia>,
    quantidadeVotos: Map<Long, Int>,
    votosUsuario: Set<Long>,
    podeVotarEmIdeia: Boolean,
    podeEditarExcluirIdeia: Boolean,
    usuarioIdLogado: Long?,
    podeAtualizarStatusIdeia: Boolean,
    podeAvaliarIdeia: Boolean,
    podeSolicitarAvaliacaoIa: Boolean,
    onAlternarVotoClick: (Long) -> Unit,
    onAtualizarStatusClick: (Long, StatusIdeia) -> Unit,
    onAvaliarIdeiaClick: (Ideia) -> Unit,
    onSolicitarAvaliacaoIaClick: (Ideia) -> Unit,
    onEditarIdeiaClick: (Long) -> Unit,
    onExcluirIdeiaClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ideias, key = { it.id }) { ideia ->
            IdeiaCard(
                ideia = ideia,
                quantidadeVotos = quantidadeVotos[ideia.id] ?: 0,
                usuarioJaVotou = votosUsuario.contains(ideia.id),
                podeVotarEmIdeia = podeVotarEmIdeia,
                podeEditarExcluirIdeia = podeEditarIdeiaNoApp(
                    roleAllowsEdit = podeEditarExcluirIdeia,
                    loggedUserId = usuarioIdLogado,
                    ideaOwnerId = ideia.usuarioId
                ),
                podeAtualizarStatusIdeia =
                    podeAtualizarStatusIdeia,
                podeAvaliarIdeia = podeAvaliarIdeia,
                podeSolicitarAvaliacaoIa = podeSolicitarAvaliacaoIa,
                onAlternarVotoClick = onAlternarVotoClick,
                onAtualizarStatus = onAtualizarStatusClick,
                onAvaliarClick = onAvaliarIdeiaClick,
                onSolicitarAvaliacaoIaClick = onSolicitarAvaliacaoIaClick,
                onEditarClick = onEditarIdeiaClick,
                onExcluirClick = onExcluirIdeiaClick
            )

        }
    }
}

internal fun podeEditarIdeiaNoApp(
    roleAllowsEdit: Boolean,
    loggedUserId: Long?,
    ideaOwnerId: Long
): Boolean {
    return roleAllowsEdit &&
        loggedUserId != null &&
        loggedUserId == ideaOwnerId
}

@Composable
private fun IdeiaCard(
    ideia: Ideia,
    quantidadeVotos: Int,
    usuarioJaVotou: Boolean,
    podeVotarEmIdeia: Boolean,
    podeEditarExcluirIdeia: Boolean,
    podeAtualizarStatusIdeia: Boolean,
    podeAvaliarIdeia: Boolean,
    podeSolicitarAvaliacaoIa: Boolean,
    onAvaliarClick: (Ideia) -> Unit,
    onSolicitarAvaliacaoIaClick: (Ideia) -> Unit,
    onAlternarVotoClick: (Long) -> Unit,
    onAtualizarStatus: (Long, StatusIdeia) -> Unit,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }
    var detalhesExpanded by remember { mutableStateOf(false) }

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

                    if (podeEditarExcluirIdeia) {
                        Box {
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
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                shadowElevation = 4.dp
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ideia.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (podeVotarEmIdeia) {
                        IconButton(onClick = { onAlternarVotoClick(ideia.id) }) {
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
                    } else {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Votos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = quantidadeVotos.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = {
                        detalhesExpanded = !detalhesExpanded
                    }
                ) {
                    Text(
                        text = if (detalhesExpanded) {
                            "Ocultar detalhes"
                        } else {
                            "Ver detalhes"
                        }
                    )

                    Icon(
                        imageVector = if (detalhesExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null
                    )
                }
            }

            if (detalhesExpanded) {
                InfoText(
                    label = "Problema",
                    value = ideia.problema
                )

                InfoText(
                    label = "Solução proposta",
                    value = ideia.solucaoProposta
                )

                InfoText(
                    label = "Orientação",
                    value = ideia.tituloOrientacao
                )

                InfoText(
                    label = "Autor",
                    value = ideia.nomeUsuario
                )

                InfoText(
                    label = "Categoria",
                    value = ideia.nomeCategoria
                )
                InfoText(
                    label = "Impacto",
                    value = ideia.impacto?.let { "$it/10" }
                        ?: "Não avaliado"
                )

                InfoText(
                    label = "Viabilidade",
                    value = ideia.viabilidade?.let { "$it/10" }
                        ?: "Não avaliada"
                )

                InfoText(
                    label = "Prioridade",
                    value = ideia.prioridade.toPlainString()
                )

                if (podeAvaliarIdeia) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            onAvaliarClick(ideia)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (
                                ideia.impacto == null ||
                                ideia.viabilidade == null
                            ) {
                                "Avaliar ideia"
                            } else {
                                "Editar avaliação"
                            }
                        )
                    }
                }

                if (podeSolicitarAvaliacaoIa) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onSolicitarAvaliacaoIaClick(ideia)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Analisar com IA")
                    }
                }
            }

            if (podeAtualizarStatusIdeia) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
}

@Composable
private fun AvaliacaoIdeiaDialog(
    state: AvaliacaoIdeiaUiState,
    onImpactoChange: (String) -> Unit,
    onViabilidadeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isSaving) {
                onDismiss()
            }
        },
        title = {
            Text("Avaliar ideia")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Informe valores entre 0 e 10."
                )

                OutlinedTextField(
                    value = state.impacto,
                    onValueChange = onImpactoChange,
                    label = {
                        Text("Impacto")
                    },
                    enabled = !state.isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.viabilidade,
                    onValueChange = onViabilidadeChange,
                    label = {
                        Text("Viabilidade")
                    },
                    enabled = !state.isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isSaving
            ) {
                Text(
                    if (state.isSaving) {
                        "Salvando..."
                    } else {
                        "Salvar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSaving
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun AiAvaliacaoIdeiaDialog(
    state: AiAvaliacaoIdeiaUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isLoading) {
                onDismiss()
            }
        },
        title = {
            Text("Avaliação por IA")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.tituloIdeia.isNotBlank()) {
                    Text(
                        text = state.tituloIdeia,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                when {
                    state.isLoading -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                            Text("Analisando a ideia...")
                        }
                    }

                    state.errorMessage != null -> {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    state.resultado != null -> {
                        val result = state.resultado

                        AiScoreRow(
                            label = "Alinhamento estratégico",
                            value = "${result.strategicAlignment}/10"
                        )
                        AiScoreRow(
                            label = "Impacto",
                            value = "${result.impact}/10"
                        )
                        AiScoreRow(
                            label = "Viabilidade",
                            value = "${result.feasibility}/10"
                        )
                        AiScoreRow(
                            label = "Inovação",
                            value = "${result.innovation}/10"
                        )
                        AiScoreRow(
                            label = "Pontuação geral",
                            value = "${result.overallScore.toPlainString()}/10"
                        )

                        Text(
                            text = "Resumo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = result.summary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Esta análise apoia a decisão do gestor e não altera o status da ideia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            when {
                state.isLoading -> {
                    TextButton(
                        onClick = {},
                        enabled = false
                    ) {
                        Text("Aguarde")
                    }
                }

                state.errorMessage != null -> {
                    Button(onClick = onRetry) {
                        Text("Tentar novamente")
                    }
                }

                else -> {
                    Button(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        },
        dismissButton = {
            if (state.errorMessage != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
private fun AiScoreRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoText(
    label: String,
    value: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold)
            ) {
                append("$label: ")
            }

            append(value)
        },
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
