package br.com.fiap.aguiaexpress.ui.screens.ideias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.domain.model.Categoria
import br.com.fiap.aguiaexpress.domain.model.Orientacao
import br.com.fiap.aguiaexpress.ui.components.ErrorMessage
import br.com.fiap.aguiaexpress.ui.components.LoadingIndicator
import br.com.fiap.aguiaexpress.viewmodel.IdeiaViewModel

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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Editar Ideia",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.titulo,
                    onValueChange = viewModel::atualizarTituloEdicao,
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = viewModel::atualizarDescricaoEdicao,
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.problema,
                    onValueChange = viewModel::atualizarProblemaEdicao,
                    label = { Text("Problema") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.solucaoProposta,
                    onValueChange = viewModel::atualizarSolucaoPropostaEdicao,
                    label = { Text("Solução proposta") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.successMessage != null) {
                    Text(
                        text = uiState.successMessage ?: "",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        viewModel.editarIdeia()
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator()
                    } else {
                        Text("Salvar alterações")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onVoltarClick,
                    modifier = Modifier.fillMaxWidth()
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