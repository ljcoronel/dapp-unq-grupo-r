package com.dappunq.config;

import com.dappunq.service.ApiKeyService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCommandConfig {
    @Bean
    @ConditionalOnProperty(name = "app.command", havingValue = "rotate-scraping-api-key")
    public ApplicationRunner rotateScrapingApiKeyCommand(ApiKeyService apiKeyService) {
        return args -> {
            try {
                System.out.println(apiKeyService.rotateScrapingApiKey());
            } catch (Exception ex) {
                System.err.println("Error: no se pudo rotar la API key de scraping");
                System.err.println("Detalle: " + describeFailure(ex));
                System.exit(1);
            }
        };
    }

    private String describeFailure(Throwable failure) {
        StringBuilder details = new StringBuilder(failure.getClass().getSimpleName());
        if (failure.getMessage() != null && !failure.getMessage().isBlank()) {
            details.append(": ").append(failure.getMessage());
        }

        Throwable cause = failure.getCause();
        while (cause != null) {
            details.append(" | causa: ").append(cause.getClass().getSimpleName());
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                details.append(": ").append(cause.getMessage());
            }
            cause = cause.getCause();
        }
        return details.toString();
    }
}
