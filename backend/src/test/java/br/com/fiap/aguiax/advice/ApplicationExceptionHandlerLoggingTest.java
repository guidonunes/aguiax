package br.com.fiap.aguiax.advice;

import br.com.fiap.aguiax.dto.ErroRespostaDTO;
import br.com.fiap.aguiax.exception.InvalidRequestException;
import br.com.fiap.aguiax.exception.PriorityCalculationUnavailableException;
import br.com.fiap.aguiax.exception.UnauthorizedActionException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationExceptionHandlerLoggingTest {

    private ApplicationExceptionHandler handler;
    private Logger handlerLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        handler = new ApplicationExceptionHandler();
        handlerLogger = (Logger) LoggerFactory.getLogger(
                ApplicationExceptionHandler.class
        );
        logAppender = new ListAppender<>();
        logAppender.start();
        handlerLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        handlerLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void authenticationFailureReturns401AndDoesNotLogCredentials() {
        MockHttpServletRequest request = request("POST", "/auth/login");

        ResponseEntity<ErroRespostaDTO> response =
                handler.handleAuthenticationFailure(
                        new BadCredentialsException(
                                "password=never-log-this-password"
                        ),
                        request
                );

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Credenciais inválidas.", response.getBody().message());
        assertLog(
                Level.WARN,
                "Falha de autenticação: method=POST, path=/auth/login, "
                        + "failureType=BadCredentialsException"
        );
        assertLogsDoNotContain("never-log-this-password", "password=");
    }

    @Test
    void forbiddenAndInvalidWorkflowAttemptsHaveSafeContext() {
        ResponseEntity<ErroRespostaDTO> forbidden =
                handler.handleUnauthorizedAction(
                        new UnauthorizedActionException(
                                "Apenas gestores podem cadastrar projetos."
                        ),
                        request("POST", "/projetos")
                );

        ResponseEntity<ErroRespostaDTO> invalid = handler.handleInvalidRequest(
                new InvalidRequestException(
                        "Transição de status não permitida: NOVA -> APROVADA."
                ),
                request("PATCH", "/ideias/42/status")
        );

        assertEquals(403, forbidden.getStatusCode().value());
        assertEquals(400, invalid.getStatusCode().value());
        assertLog(
                Level.WARN,
                "Operação proibida rejeitada: method=POST, path=/projetos, "
                        + "reason=Apenas gestores podem cadastrar projetos."
        );
        assertLog(
                Level.WARN,
                "Operação inválida rejeitada: method=PATCH, "
                        + "path=/ideias/42/status, reason=Transição de status "
                        + "não permitida: NOVA -> APROVADA."
        );
        assertLogsDoNotContain("Authorization", "Bearer");
    }

    @Test
    void databaseFailureReturnsGeneric503WithoutLoggingConnectionDetails() {
        String sensitiveConnectionDetails =
                "jdbc:mysql://localhost/aguiax?password=db-secret";

        ResponseEntity<ErroRespostaDTO> response =
                handler.handleDataAccessFailure(
                        new DataAccessResourceFailureException(
                                sensitiveConnectionDetails
                        ),
                        request("GET", "/projetos")
                );

        assertEquals(503, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "Não foi possível acessar os dados neste momento.",
                response.getBody().message()
        );
        assertLog(
                Level.ERROR,
                "Falha de acesso ao banco: method=GET, path=/projetos, "
                        + "failureType=DataAccessResourceFailureException"
        );
        assertLogsDoNotContain(sensitiveConnectionDetails, "db-secret");
    }

    @Test
    void priorityPersistenceFailureDoesNotLogItsDatabaseCause() {
        PriorityCalculationUnavailableException exception =
                new PriorityCalculationUnavailableException(
                        "Não foi possível persistir a prioridade da ideia.",
                        new DataAccessResourceFailureException(
                                "mongodb://user:db-secret@localhost"
                        )
                );

        ResponseEntity<ErroRespostaDTO> response =
                handler.handlePriorityCalculationUnavailable(
                        exception,
                        request("POST", "/api/ideas/42/ai-evaluation")
                );

        assertEquals(503, response.getStatusCode().value());
        assertLog(
                Level.ERROR,
                "Falha ao calcular ou persistir prioridade: method=POST, "
                        + "path=/api/ideas/42/ai-evaluation, "
                        + "failureType=PriorityCalculationUnavailableException"
        );
        assertLogsDoNotContain("mongodb://", "db-secret");
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(path);
        return request;
    }

    private void assertLog(Level level, String expectedMessage) {
        assertTrue(logAppender.list.stream().anyMatch(event ->
                event.getLevel() == level
                        && expectedMessage.equals(event.getFormattedMessage())
        ));
    }

    private void assertLogsDoNotContain(String... sensitiveValues) {
        List<String> messages = logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        for (String sensitiveValue : sensitiveValues) {
            assertFalse(messages.stream().anyMatch(message ->
                    message.contains(sensitiveValue)
            ));
        }
    }
}
