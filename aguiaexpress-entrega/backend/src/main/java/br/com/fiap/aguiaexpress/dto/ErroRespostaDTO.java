package br.com.fiap.aguiaexpress.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErroRespostaDTO(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<String> details
) {
}