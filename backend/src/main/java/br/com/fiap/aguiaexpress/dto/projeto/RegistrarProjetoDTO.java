package br.com.fiap.aguiaexpress.dto.projeto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dados do projeto criado a partir de uma ideia aprovada")
public record RegistrarProjetoDTO(

        @NotNull(message = "O ID da ideia é obrigatório.")
        @Schema(example = "42")
        Long ideiaId,

        @NotNull(message = "O ID do usuário responsável é obrigatório.")
        @Schema(example = "7")
        Long usuarioId,

        @NotBlank(message = "O título do projeto é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        @Schema(example = "Projeto de eficiência logística")
        String titulo,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @Size(max = 1000, message = "O objetivo deve ter no máximo 1000 caracteres.")
        String objetivo,

        @DecimalMin(value = "0.0", message = "O investimento não pode ser negativo.")
        @Schema(minimum = "0", example = "50000.00")
        BigDecimal investimento,

        @DecimalMin(value = "0.0", message = "O ROI não pode ser negativo.")
        @Schema(minimum = "0", example = "18.50")
        BigDecimal roi,

        @DecimalMin(value = "0.0", message = "O retorno financeiro não pode ser negativo.")
        @Schema(minimum = "0", example = "80000.00")
        BigDecimal retornoFinanceiro,

        @DecimalMin(value = "0.0", message = "A redução de custos não pode ser negativa.")
        @Schema(minimum = "0", example = "12000.00")
        BigDecimal reducaoCustos,

        @DecimalMin(value = "0.0", message = "O ganho de produtividade não pode ser negativo.")
        @Schema(minimum = "0", example = "9.50")
        BigDecimal ganhoProdutividade,

        LocalDate dataInicio,

        LocalDate dataPrevisaoFim,

        LocalDate dataConclusao

) {
}
