package br.com.fiap.aguiaexpress.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aguiaExpressOpenAPI() {
        //http://localhost:8080/swagger-ui/index.html
        return new OpenAPI()
                .info(new Info()
                        .title("AguiaExpress API")
                        .description("API REST do projeto acadêmico AguiaExpress, integrada ao aplicativo Android em Kotlin/Jetpack Compose.")
                        .version("v1.0.0"));
    }
}