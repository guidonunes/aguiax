package br.com.fiap.aguiaexpress.config.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai")
public record AiProviderProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String model,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {

    private static final String DEFAULT_PROVIDER = "gemini";
    private static final String DEFAULT_MODEL = "gemini-3.6-flash";
    private static final String DEFAULT_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta";

    private static final Duration DEFAULT_CONNECT_TIMEOUT =
            Duration.ofSeconds(3);

    private static final Duration DEFAULT_READ_TIMEOUT =
            Duration.ofSeconds(15);

    public AiProviderProperties {
        provider = defaultIfBlank(provider, DEFAULT_PROVIDER);
        apiKey = normalizeOptional(apiKey);
        model = defaultIfBlank(model, DEFAULT_MODEL);
        baseUrl = defaultIfBlank(baseUrl, DEFAULT_BASE_URL);

        connectTimeout = connectTimeout == null
                ? DEFAULT_CONNECT_TIMEOUT
                : connectTimeout;

        readTimeout = readTimeout == null
                ? DEFAULT_READ_TIMEOUT
                : readTimeout;
    }

    private static String defaultIfBlank(
            String value,
            String defaultValue
    ) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
