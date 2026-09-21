package br.com.fiap.aguiax.exception;

public class PriorityCalculationUnavailableException
        extends RuntimeException {

    public PriorityCalculationUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
