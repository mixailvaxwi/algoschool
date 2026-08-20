package com.algoschool.config;

import lombok.Getter;

import java.util.List;

/**
 * Не заданы обязательные переменные окружения.
 * <p>
 * Отдельный тип нужен, чтобы {@link MissingConfigurationFailureAnalyzer} мог
 * показать понятное сообщение вместо стектрейса на сорок строк.
 */
@Getter
public class MissingConfigurationException extends RuntimeException {

    private final List<String> missingVariables;

    public MissingConfigurationException(List<String> missingVariables) {
        super("Не заданы переменные окружения: " + String.join(", ", missingVariables));
        this.missingVariables = missingVariables;
    }
}
