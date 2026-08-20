package com.algoschool.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Проверяет обязательные переменные окружения до создания бинов.
 * <p>
 * Без этой проверки отсутствие переменной всплывало глубоко в цепочке
 * инициализации: «Error creating bean with name 'jwtAuthenticationFilter'…»,
 * и настоящая причина оказывалась в самом низу стектрейса. Вдобавок
 * переменные обнаруживались по одной — каждая следующая только после
 * перезапуска.
 * <p>
 * Здесь проверяются все сразу, до старта контекста.
 */
public class RequiredEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /** Свойство -> переменная окружения, из которой оно берётся. */
    private static final Map<String, String> REQUIRED = new LinkedHashMap<>();

    static {
        REQUIRED.put("spring.datasource.password", "DB_PASSWORD");
        REQUIRED.put("app.jwt.secret", "JWT_SECRET");
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<String> missing = new ArrayList<>();

        REQUIRED.forEach((property, variable) -> {
            if (isBlankOrUnresolved(environment, property)) {
                missing.add(variable);
            }
        });

        if (!missing.isEmpty()) {
            throw new MissingConfigurationException(missing);
        }
    }

    private boolean isBlankOrUnresolved(ConfigurableEnvironment environment, String property) {
        String value;
        try {
            value = environment.getProperty(property);
        } catch (IllegalArgumentException e) {
            // Плейсхолдер не разрешился — переменной нет
            return true;
        }
        // Binder у @ConfigurationProperties не падает на неразрешённом плейсхолдере,
        // а оставляет его текстом, поэтому проверяем и такой случай.
        return value == null || value.isBlank() || value.startsWith("${");
    }
}
