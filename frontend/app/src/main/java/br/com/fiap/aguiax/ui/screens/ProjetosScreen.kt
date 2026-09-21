package br.com.fiap.aguiax.ui.screens.projetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import br.com.fiap.aguiax.ui.components.AguiaTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiax.domain.model.Projeto
import br.com.fiap.aguiax.ui.components.EmptyState
import br.com.fiap.aguiax.ui.components.ErrorMessage
import br.com.fiap.aguiax.ui.components.LoadingIndicator
import br.com.fiap.aguiax.ui.components.StatusTag
import br.com.fiap.aguiax.ui.theme.AguiaXTheme
import br.com.fiap.aguiax.viewmodel.ProjetoViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner


@Composable
fun ProjetosScreen(
    viewModel: ProjetoViewModel = viewModel(),
    podeGerenciarProjetos: Boolean,
    onEditarProjetoClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var projetoIdParaExcluir by remember { mutableStateOf<Long?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.carregarProjetos()
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
            text = "Projetos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Acompanhe projetos gerados a partir das ideias aprovadas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando projetos...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar projetos",
                    onRetry = {
                        viewModel.carregarProjetos()
                    }
                )
            }

            uiState.projetos.isEmpty() -> {
                EmptyState(message = "Nenhum projeto encontrado.")
            }

            else -> {
                ProjetoList(
                    projetos = uiState.projetos,
                    podeGerenciarProjetos = podeGerenciarProjetos,
                    onEditarClick = { projetoId ->
                        onEditarProjetoClick(projetoId)
                    },
                    onExcluirClick = { projetoId ->
                        projetoIdParaExcluir = projetoId
                    }
                )
            }
        }
    }
    if (projetoIdParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                projetoIdParaExcluir = null
            },
            title = {
                Text(text = "Excluir projeto")
            },
            text = {
                Text(
                    text = "Tem certeza que deseja excluir este projeto? Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        projetoIdParaExcluir?.let { projetoId ->
                            viewModel.deletarProjeto(projetoId)
                        }

                        projetoIdParaExcluir = null
                    }
                ) {
                    Text(text = "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        projetoIdParaExcluir = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProjetoList(
    projetos: List<Projeto>,
    podeGerenciarProjetos: Boolean,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(projetos) { projeto ->
            ProjetoCard(
                projeto = projeto,
                podeGerenciarProjetos = podeGerenciarProjetos,
                onEditarClick = onEditarClick,
                onExcluirClick = onExcluirClick
            )
        }
    }
}

@Composable
private fun ProjetoCard(
    projeto: Projeto,
    podeGerenciarProjetos: Boolean,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {
    var detalhesExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

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
                    text = projeto.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusTag(text = formatarStatusProjeto(projeto.statusProjeto))

                    if (podeGerenciarProjetos) {
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
                                        onEditarClick(projeto.id)
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Excluir")
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onExcluirClick(projeto.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = projeto.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
                    label = "Objetivo",
                    value = projeto.objetivo
                )

                InfoText(
                    label = "Ideia vinculada",
                    value = projeto.tituloIdeia
                )

                InfoText(
                    label = "Responsável",
                    value = projeto.nomeUsuarioResponsavel
                )

                InfoText(
                    label = "Investimento",
                    value = "R$ ${projeto.investimento}"
                )

                InfoText(
                    label = "ROI",
                    value = "${projeto.roi}%"
                )

                InfoText(
                    label = "Retorno financeiro",
                    value = "R$ ${projeto.retornoFinanceiro}"
                )

                InfoText(
                    label = "Redução de custos",
                    value = "R$ ${projeto.reducaoCustos}"
                )

                InfoText(
                    label = "Ganho de produtividade",
                    value = "${projeto.ganhoProdutividade}%"
                )

                InfoText(
                    label = "Data de início",
                    value = projeto.dataInicio
                )

                InfoText(
                    label = "Previsão de fim",
                    value = projeto.dataPrevisaoFim
                )

                if (projeto.dataConclusao != null) {
                    InfoText(
                        label = "Data de conclusão",
                        value = projeto.dataConclusao
                    )
                }
            }
        }
    }
}

private fun formatarStatusProjeto(status: String): String {
    return when (status) {
        "PLANEJADO" -> "Planejado"
        "EM_ANDAMENTO" -> "Em andamento"
        "CONCLUIDO" -> "Concluído"
        "CANCELADO" -> "Cancelado"
        else -> status
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

@Preview(showBackground = true)
@Composable
private fun ProjetosScreenPreview() {
    AguiaXTheme() {
        ProjetosScreen(
            podeGerenciarProjetos = true,
            onEditarProjetoClick = {}
        )
    }
}
