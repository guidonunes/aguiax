package br.com.fiap.aguiaexpress;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AguiaexpressApplication {

    public static void main(String[] args) {
        carregarConfiguracaoLocal();

        SpringApplication.run(AguiaexpressApplication.class, args);
    }

    private static void carregarConfiguracaoLocal() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        definirPropriedadeSePresente(dotenv, "DB_URL");
        definirPropriedadeSePresente(dotenv, "DB_USER");
        definirPropriedadeSePresente(dotenv, "DB_PASSWORD");
        definirPropriedadeSePresente(dotenv, "JWT_SECRET");
        definirPropriedadeSePresente(dotenv, "MONGODB_URI");
        definirPropriedadeSePresente(dotenv, "MONGODB_DATABASE");
    }

    private static void definirPropriedadeSePresente(
            Dotenv dotenv,
            String nome
    ) {
        if (System.getProperty(nome) != null) {
            return;
        }

        String valor = dotenv.get(nome, System.getenv(nome));

        if (valor != null && !valor.isBlank()) {
            System.setProperty(nome, valor);
        }
    }
}
