package br.com.fiap.aguiaexpress.ui.screens.orientacoes

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.ui.components.EmptyState
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.OrientacaoViewModel
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.ui.components.PerfilTag
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import br.com.fiap.aguiaexpress.ui.components.AguiaTextButton as TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner


@Composable
fun OrientacoesScreen(
    podeGerenciarOrientacoes: Boolean,
    onCriarOrientacaoClick: () -> Unit,
    onEditarOrientacaoClick: (Long) -> Unit,
    viewModel: OrientacaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var orientacaoIdParaExcluir by remember { mutableStateOf<Long?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.carregarOrientacoes()
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
            text = "Orientações Estratégicas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consulte as diretrizes que ajudam a guiar ideias e projetos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Carregando orientações...")
            }

            uiState.errorMessage != null -> {
                ErrorMessage(
                    message = uiState.errorMessage ?: "Erro ao carregar orientações",
                    onRetry = {
                        viewModel.carregarOrientacoes()
                    }
                )
            }

            uiState.orientacoes.isEmpty() -> {
                EmptyState(message = "Nenhuma orientação encontrada.")
            }

            else -> {
                OrientacaoList(
                    orientacoes = uiState.orientacoes,
                    onEditarClick = { orientacaoId ->
                        onEditarOrientacaoClick(orientacaoId)
                    },
                    onExcluirClick = { orientacaoId ->
                        orientacaoIdParaExcluir = orientacaoId
                    },
                    podeGerenciarOrientacoes = podeGerenciarOrientacoes
                )
            }
        }
    }
    if (orientacaoIdParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                orientacaoIdParaExcluir = null
            },
            title = {
                Text(text = "Excluir orientação")
            },
            text = {
                Text(
                    text = "Tem certeza que deseja excluir esta orientação estratégica? Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton (
                    onClick = {
                        orientacaoIdParaExcluir?.let { orientacaoId ->
                            viewModel.deletarOrientacao(orientacaoId)
                        }

                        orientacaoIdParaExcluir = null
                    }
                ) {
                    Text(text = "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        orientacaoIdParaExcluir = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OrientacaoList(
    orientacoes: List<Orientacao>,
    podeGerenciarOrientacoes: Boolean,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orientacoes) { orientacao ->
            OrientacaoCard(
                orientacao = orientacao,
                podeGerenciarOrientacoes = podeGerenciarOrientacoes,
                onEditarClick = onEditarClick,
                onExcluirClick = onExcluirClick
            )
        }
    }
}

@Composable
private fun OrientacaoCard(
    orientacao: Orientacao,
    podeGerenciarOrientacoes: Boolean,
    onEditarClick: (Long) -> Unit,
    onExcluirClick: (Long) -> Unit
) {
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
                    text = orientacao.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (podeGerenciarOrientacoes) {
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
                                    onEditarClick(orientacao.id)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Excluir")
                                },
                                onClick = {
                                    menuExpanded = false
                                    onExcluirClick(orientacao.id)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = orientacao.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoText(
                label = "Área",
                value = orientacao.areaEstrategica
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold)
                        ) {
                            append("Criado por: ")
                        }

                        append(orientacao.nomeUsuario)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                PerfilTag(perfil = orientacao.perfilUsuario)
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
private fun OrientacoesScreenPreview() {
    AguiaExpressTheme {
        OrientacoesScreen(
            onCriarOrientacaoClick = {},
            onEditarOrientacaoClick = {},
            podeGerenciarOrientacoes = true

        )
    }

}
