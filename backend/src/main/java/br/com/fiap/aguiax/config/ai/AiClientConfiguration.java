package br.com.fiap.aguiax.config.ai;

import br.com.fiap.aguiax.ai.client.AiDashboardInsightClient;
import br.com.fiap.aguiax.ai.client.AiIdeaEvaluationClient;
import br.com.fiap.aguiax.ai.client.GeminiAiIdeaEvaluationClient;
import br.com.fiap.aguiax.ai.client.GeminiDashboardInsightClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiClientConfiguration {

    private static final String GEMINI_PROVIDER = "gemini";

    @Bean
    @ConditionalOnProperty(
            prefix = "app.ai",
            name = "enabled",
            havingValue = "true"
    )
    public AiIdeaEvaluationClient aiIdeaEvaluationClient(
            AiProviderProperties properties,
            ObjectMapper objectMapper
    ) {
        validateEnabledConfiguration(properties);

        return new GeminiAiIdeaEvaluationClient(
                properties.baseUrl(),
                properties.apiKey(),
                properties.model(),
                properties.connectTimeout(),
                properties.readTimeout(),
                objectMapper
        );
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "app.ai",
            name = "enabled",
            havingValue = "true"
    )
    public AiDashboardInsightClient aiDashboardInsightClient(
            AiProviderProperties properties,
            ObjectMapper objectMapper
    ) {
        validateEnabledConfiguration(properties);

        return new GeminiDashboardInsightClient(
                properties.baseUrl(),
                properties.apiKey(),
                properties.model(),
                properties.connectTimeout(),
                properties.readTimeout(),
                objectMapper
        );
    }

    private void validateEnabledConfiguration(
            AiProviderProperties properties
    ) {
        if (!GEMINI_PROVIDER.equalsIgnoreCase(properties.provider())) {
            throw new IllegalStateException(
                    "AI_PROVIDER não suportado: "
                            + properties.provider()
            );
        }

        if (properties.apiKey() == null) {
            throw new IllegalStateException(
                    "AI_API_KEY é obrigatória quando AI_ENABLED=true"
            );
        }
    }
}
