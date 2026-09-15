package br.com.fiap.aguiaexpress.ui.screens.projetos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import br.com.fiap.aguiaexpress.ui.components.AguiaButton as Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import br.com.fiap.aguiaexpress.ui.theme.semanticColors
import br.com.fiap.aguiaexpress.ui.components.AguiaOutlinedButton as OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiaexpress.viewmodel.ProjetoViewModel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.com.fiap.aguiaexpress.domain.model.Ideia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarProjetoScreen(
    onVoltarClick: () -> Unit,
    viewModel: ProjetoViewModel = viewModel()
) {
    val uiState by viewModel.criarProjetoUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "Cadastrar Projeto",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Transforme uma ideia aprovada em uma iniciativa acompanhável.",
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
                FormSectionTitle(text = "Ideia vinculada")

                IdeiaDropdown(
                    ideias = uiState.ideias,
                    selectedIdeiaId = uiState.ideiaId.toLongOrNull(),
                    onIdeiaSelected = { ideiaId ->
                        viewModel.atualizarIdeiaIdCriacao(ideiaId.toString())
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                FormSectionTitle(text = "Informações principais")

                FormTextField(
                    value = uiState.titulo,
                    onValueChange = viewModel::atualizarTituloCriacao,
                    label = "Título",
                    placeholder = "Ex: Painel de indicadores gerenciais"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.descricao,
                    onValueChange = viewModel::atualizarDescricaoCriacao,
                    label = "Descrição",
                    placeholder = "Descreva brevemente o projeto",
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.objetivo,
                    onValueChange = viewModel::atualizarObjetivoCriacao,
                    label = "Objetivo",
                    placeholder = "Qual resultado esse projeto pretende alcançar?",
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))

                FormSectionTitle(text = "Métricas financeiras")

                FormTextField(
                    value = uiState.investimento,
                    onValueChange = viewModel::atualizarInvestimentoCriacao,
                    label = "Investimento",
                    placeholder = "Ex: 18000.00"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.roi,
                    onValueChange = viewModel::atualizarRoiCriacao,
                    label = "ROI (%)",
                    placeholder = "Ex: 16.75"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.retornoFinanceiro,
                    onValueChange = viewModel::atualizarRetornoFinanceiroCriacao,
                    label = "Retorno financeiro",
                    placeholder = "Ex: 38000.00"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.reducaoCustos,
                    onValueChange = viewModel::atualizarReducaoCustosCriacao,
                    label = "Redução de custos",
                    placeholder = "Ex: 9000.00"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.ganhoProdutividade,
                    onValueChange = viewModel::atualizarGanhoProdutividadeCriacao,
                    label = "Ganho de produtividade (%)",
                    placeholder = "Ex: 17.50"
                )

                Spacer(modifier = Modifier.height(24.dp))

                FormSectionTitle(text = "Datas")

                FormTextField(
                    value = uiState.dataInicio,
                    onValueChange = viewModel::atualizarDataInicioCriacao,
                    label = "Data de início",
                    placeholder = "Ex: 2026-07-01"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.dataPrevisaoFim,
                    onValueChange = viewModel::atualizarDataPrevisaoFimCriacao,
                    label = "Previsão de fim",
                    placeholder = "Ex: 2026-10-01"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FormTextField(
                    value = uiState.dataConclusao,
                    onValueChange = viewModel::atualizarDataConclusaoCriacao,
                    label = "Data de conclusão",
                    placeholder = "Opcional. Ex: 2026-09-20"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                viewModel.criarProjeto()
            },
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.semanticColors.primaryAction,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                Text(text = "Cadastrar projeto")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onVoltarClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Voltar")
        }

        Spacer(modifier = Modifier.height(32.dp))
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

@Composable
private fun FeedbackMessage(
    message: String,
    isError: Boolean
) {
    val backgroundColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.semanticColors.successContainer
    }

    val textColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.semanticColors.onSuccessContainer
    }

    Text(
        text = message,
        color = textColor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeiaDropdown(
    ideias: List<Ideia>,
    selectedIdeiaId: Long?,
    onIdeiaSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedIdeia = ideias.find { ideia ->
        ideia.id == selectedIdeiaId
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedIdeia?.titulo ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Ideia vinculada")
            },
            placeholder = {
                Text("Selecione uma ideia")
            },
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
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ideias.forEach { ideia ->
                DropdownMenuItem(
                    text = {
                        Text(text = ideia.titulo)
                    },
                    onClick = {
                        onIdeiaSelected(ideia.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
