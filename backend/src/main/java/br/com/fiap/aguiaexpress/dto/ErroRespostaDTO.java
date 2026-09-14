package br.com.fiap.aguiaexpress.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Contrato padronizado de erro da aplicação")
public record ErroRespostaDTO(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
