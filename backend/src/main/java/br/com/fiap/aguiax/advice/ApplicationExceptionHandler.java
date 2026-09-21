package br.com.fiap.aguiax.advice;

import br.com.fiap.aguiax.ai.client.AiProviderException;
import br.com.fiap.aguiax.dto.ErroRespostaDTO;
import br.com.fiap.aguiax.exception.AiEvaluationPersistenceException;
import br.com.fiap.aguiax.exception.AiEvaluationUnavailableException;
import br.com.fiap.aguiax.exception.DuplicateResourceException;
import br.com.fiap.aguiax.exception.DashboardInsightPersistenceException;
import br.com.fiap.aguiax.exception.DashboardInsightUnavailableException;
import br.com.fiap.aguiax.exception.InvalidRequestException;
import br.com.fiap.aguiax.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiax.exception.ResourceNotFoundException;
import br.com.fiap.aguiax.exception.UnauthorizedActionException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ApplicationExceptionHandler.class
    );

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
        LOGGER.warn(
                "Operação proibida rejeitada: method={}, path={}, reason={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

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
        LOGGER.warn(
                "Operação inválida rejeitada: method={}, path={}, reason={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

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

    @ExceptionHandler(AiEvaluationPersistenceException.class)
    public ResponseEntity<ErroRespostaDTO> handleAiEvaluationPersistence(
            AiEvaluationPersistenceException exception,
            HttpServletRequest request
    ) {
        return criarRespostaDeErro(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DashboardInsightUnavailableException.class)
    public ResponseEntity<ErroRespostaDTO> handleDashboardInsightUnavailable(
            DashboardInsightUnavailableException exception,
            HttpServletRequest request
    ) {
        return criarRespostaDeErro(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DashboardInsightPersistenceException.class)
    public ResponseEntity<ErroRespostaDTO> handleDashboardInsightPersistence(
            DashboardInsightPersistenceException exception,
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

    @ExceptionHandler(PriorityCalculationUnavailableException.class)
    public ResponseEntity<ErroRespostaDTO> handlePriorityCalculationUnavailable(
            PriorityCalculationUnavailableException exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Falha ao calcular ou persistir prioridade: method={}, path={}, failureType={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        return criarRespostaDeErro(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroRespostaDTO> handleAuthenticationFailure(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn(
                "Falha de autenticação: method={}, path={}, failureType={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        return criarRespostaDeErro(
                HttpStatus.UNAUTHORIZED,
                "Credenciais inválidas.",
                request
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErroRespostaDTO> handleDataAccessFailure(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Falha de acesso ao banco: method={}, path={}, failureType={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        return criarRespostaDeErro(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível acessar os dados neste momento.",
                request
        );
    }
}
