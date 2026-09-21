package br.com.fiap.aguiax.ui.screens.orientacoes

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
import br.com.fiap.aguiax.ui.components.AguiaButton as Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import br.com.fiap.aguiax.ui.theme.semanticColors
import br.com.fiap.aguiax.ui.components.AguiaOutlinedButton as OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.aguiax.ui.components.ErrorMessage
import br.com.fiap.aguiax.ui.components.LoadingIndicator
import br.com.fiap.aguiax.viewmodel.OrientacaoViewModel

@Composable
fun EditarOrientacaoScreen(
    orientacaoId: Long,
    onVoltarClick: () -> Unit,
    viewModel: OrientacaoViewModel = viewModel()
) {
    val uiState by viewModel.editarOrientacaoUiState.collectAsState()

    LaunchedEffect(orientacaoId) {
        viewModel.carregarOrientacaoParaEdicao(orientacaoId)
    }

    when {
        uiState.isLoading -> {
            LoadingIndicator(message = "Carregando orientação...")
        }

        uiState.errorMessage != null && uiState.orientacaoId == null -> {
            ErrorMessage(
                message = uiState.errorMessage ?: "Erro ao carregar orientação",
                onRetry = {
                    viewModel.carregarOrientacaoParaEdicao(orientacaoId)
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
                    text = "Editar Orientação",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Atualize as informações da orientação estratégica.",
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
                            placeholder = "Ex: Redução inteligente de custos operacionais"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FormTextField(
                            value = uiState.descricao,
                            onValueChange = viewModel::atualizarDescricaoEdicao,
                            label = "Descrição",
                            placeholder = "Descreva o objetivo dessa orientação estratégica",
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FormSectionTitle(text = "Classificação")

                        FormTextField(
                            value = uiState.areaEstrategica,
                            onValueChange = viewModel::atualizarAreaEstrategicaEdicao,
                            label = "Área estratégica",
                            placeholder = "Ex: Tecnologia, Atendimento, Operações"
                        )

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


                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        viewModel.editarOrientacao()
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
                        Text(text = "Salvar alterações")
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
