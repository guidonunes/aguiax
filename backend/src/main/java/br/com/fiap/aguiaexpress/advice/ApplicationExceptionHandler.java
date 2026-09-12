package br.com.fiap.aguiaexpress.advice;

import br.com.fiap.aguiaexpress.ai.client.AiProviderException;
import br.com.fiap.aguiaexpress.dto.ErroRespostaDTO;
import br.com.fiap.aguiaexpress.exception.AiEvaluationUnavailableException;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroRespostaDTO> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErroRespostaDTO> handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ErroRespostaDTO> handleUnauthorizedAction(
            UnauthorizedActionException exception,
            HttpServletRequest request
    ) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErroRespostaDTO> handleInvalidRequest(
            InvalidRequestException exception,
            HttpServletRequest request
    ) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(AiEvaluationUnavailableException.class)
    public ResponseEntity<ErroRespostaDTO> handleAiEvaluationUnavailable(
            AiEvaluationUnavailableException exception,
            HttpServletRequest request
    ) {
        return criarRespostaDeErro(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<ErroRespostaDTO> handleAiProviderFailure(
            AiProviderException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = switch (exception.failure()) {
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case RATE_LIMITED, UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case REJECTED_REQUEST, INVALID_RESPONSE -> HttpStatus.BAD_GATEWAY;
            case REQUEST_BUILD_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return criarRespostaDeErro(status, exception.getMessage(), request);
    }

    private ResponseEntity<ErroRespostaDTO> criarRespostaDeErro(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(status).body(erro);
    }

}
