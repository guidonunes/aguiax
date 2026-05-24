package br.com.fiap.aguiaexpress;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AguiaexpressApplication {

	public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        System.setProperty("DB_URL", dotenv.get("DB_URL", System.getenv("DB_URL")));
        System.setProperty("DB_USER", dotenv.get("DB_USER", System.getenv("DB_USER")));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", System.getenv("DB_PASSWORD")));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));

        SpringApplication.run(AguiaexpressApplication.class, args);
	}

}
