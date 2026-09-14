package br.com.fiap.aguiaexpress.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI aguiaExpressOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AguiaExpress API")
                        .description("API REST do AguiaExpress. Use o token retornado por POST /auth/login no esquema bearerAuth.")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT emitido por POST /auth/login")
                ))
                .addSecurityItem(new SecurityRequirement().addList(
                        BEARER_AUTH_SCHEME
                ));
    }
}
