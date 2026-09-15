package br.com.fiap.aguiaexpress.ui.screens.ideias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Categoria
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.ui.theme.AguiaExpressTheme
import br.com.fiap.aguiaexpress.viewmodel.IdeiaViewModel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarIdeiaScreen(
    ideiaId: Long,
    onVoltarClick: () -> Unit,
    viewModel: IdeiaViewModel = viewModel()
) {
    val uiState by viewModel.editarIdeiaUiState.collectAsState()

    LaunchedEffect(ideiaId) {
        viewModel.carregarIdeiaParaEdicao(ideiaId)
    }

    when {
        uiState.isLoading -> {
            LoadingIndicator(message = "Carregando ideia...")
        }

        uiState.errorMessage != null && uiState.ideiaId == null -> {
            ErrorMessage(
                message = uiState.errorMessage ?: "Erro ao carregar ideia",
                onRetry = {
                    viewModel.carregarIdeiaParaEdicao(ideiaId)
                }
            )
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = "Editar Ideia",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Atualize as informações da ideia cadastrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                        FormSectionTitle(text = "Informações principais")

                        FormTextField(
                            value = uiState.titulo,
                            onValueChange = viewModel::atualizarTituloEdicao,
                            label = "Título",
                            placeholder = "Ex: Reduzir tempo de atendimento interno"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FormTextField(
                            value = uiState.descricao,
                            onValueChange = viewModel::atualizarDescricaoEdicao,
                            label = "Descrição",
                            placeholder = "Descreva brevemente sua ideia",
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FormSectionTitle(text = "Detalhamento da ideia")

                        FormTextField(
                            value = uiState.problema,
                            onValueChange = viewModel::atualizarProblemaEdicao,
                            label = "Problema",
                            placeholder = "Qual problema essa ideia resolve?",
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FormTextField(
                            value = uiState.solucaoProposta,
                            onValueChange = viewModel::atualizarSolucaoPropostaEdicao,
                            label = "Solução proposta",
                            placeholder = "Como essa ideia pode ser aplicada?",
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FormSectionTitle(text = "Classificação")

                        CategoriaDropdownEdicao(
                            categorias = uiState.categorias,
                            selectedCategoriaId = uiState.categoriaId,
                            onCategoriaSelected = viewModel::atualizarCategoriaIdEdicao
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OrientacaoDropdownEdicao(
                            orientacoes = uiState.orientacoes,
                            selectedOrientacaoId = uiState.orientacaoId,
                            onOrientacaoSelected = viewModel::atualizarOrientacaoIdEdicao
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))



                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.errorMessage != null) {
                    FeedbackMessage(
                        message = uiState.errorMessage ?: "",
                        isError = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.successMessage != null) {
                    FeedbackMessage(
                        message = uiState.successMessage ?: "",
                        isError = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        viewModel.editarIdeia()
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Salvar alterações")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onVoltarClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Voltar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriaDropdownEdicao(
    categorias: List<Categoria>,
    selectedCategoriaId: Long?,
    onCategoriaSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedCategoria = categorias.find { categoria ->
        categoria.id == selectedCategoriaId
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedCategoria?.nome ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = {
                        Text(categoria.nome)
                    },
                    onClick = {
                        onCategoriaSelected(categoria.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrientacaoDropdownEdicao(
    orientacoes: List<Orientacao>,
    selectedOrientacaoId: Long?,
    onOrientacaoSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedOrientacao = orientacoes.find { orientacao ->
        orientacao.id == selectedOrientacaoId
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedOrientacao?.titulo ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Orientação Estratégica") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            orientacoes.forEach { orientacao ->
                DropdownMenuItem(
                    text = {
                        Text(orientacao.titulo)
                    },
                    onClick = {
                        onOrientacaoSelected(orientacao.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FormSectionTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        placeholder = {
            Text(text = placeholder)
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines
    )
}

@Preview(showBackground = true)
@Composable
private fun EditarIdeiaScreenPreview() {
    AguiaExpressTheme() {
        EditarIdeiaScreen(
            ideiaId = 1,
            onVoltarClick = {}
        )
    }
}
