package br.com.fiap.aguiaexpress.ai.client;

import java.util.Objects;

public final class AiProviderException extends RuntimeException {

    private final AiProviderFailure failure;

    public AiProviderException(
            AiProviderFailure failure,
            String message
    ) {
        super(message);
        this.failure = Objects.requireNonNull(failure);
    }

    public AiProviderException(
            AiProviderFailure failure,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.failure = Objects.requireNonNull(failure);
    }

    public AiProviderFailure failure() {
        return failure;
    }
}
