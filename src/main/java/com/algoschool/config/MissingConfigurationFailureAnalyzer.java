package com.algoschool.config;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Печатает понятный блок «APPLICATION FAILED TO START» вместо стектрейса,
 * когда не заданы обязательные переменные окружения.
 */
public class MissingConfigurationFailureAnalyzer extends AbstractFailureAnalyzer<MissingConfigurationException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MissingConfigurationException cause) {
        String description = "Не заданы обязательные переменные окружения: "
                + String.join(", ", cause.getMissingVariables());

        StringBuilder action = new StringBuilder();
        action.append("Задайте их и запустите снова.\n\n");
        action.append("IntelliJ IDEA: Run -> Edit Configurations -> поле \"Environment variables\",\n");
        action.append("значения через точку с запятой, например:\n");
        action.append("    JWT_SECRET=<ключ>;DB_PASSWORD=<пароль>\n\n");
        action.append("Терминал:\n");
        action.append("    cp .env.example .env   # заполнить значения\n");
        action.append("    set -a && . ./.env && set +a && ./mvnw spring-boot:run\n");

        if (cause.getMissingVariables().contains("JWT_SECRET")) {
            action.append("\nJWT_SECRET — строка Base64 не короче 32 байт. Сгенерировать:\n");
            action.append("    openssl rand -base64 32\n");
        }

        return new FailureAnalysis(description, action.toString(), cause);
    }
}
