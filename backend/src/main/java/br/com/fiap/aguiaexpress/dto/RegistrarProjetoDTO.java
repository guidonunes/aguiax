package br.com.fiap.aguiaexpress.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarProjetoDTO(

        @NotNull(message = "O ID da ideia é obrigatório.")
        Long ideiaId,

        @NotNull(message = "O ID do usuário responsável é obrigatório.")
        Long usuarioId,

        @NotBlank(message = "O título do projeto é obrigatório.")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
        String titulo,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @Size(max = 1000, message = "O objetivo deve ter no máximo 1000 caracteres.")
        String objetivo,

        @DecimalMin(value = "0.0", message = "O investimento não pode ser negativo.")
        BigDecimal investimento,

        @DecimalMin(value = "0.0", message = "O ROI não pode ser negativo.")
        BigDecimal roi,

        @DecimalMin(value = "0.0", message = "O retorno financeiro não pode ser negativo.")
        BigDecimal retornoFinanceiro,

        @DecimalMin(value = "0.0", message = "A redução de custos não pode ser negativa.")
        BigDecimal reducaoCustos,

        @DecimalMin(value = "0.0", message = "O ganho de produtividade não pode ser negativo.")
        BigDecimal ganhoProdutividade,

        LocalDate dataInicio,

        LocalDate dataPrevisaoFim,

        LocalDate dataConclusao

) {
}