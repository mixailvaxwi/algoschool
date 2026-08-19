package com.algoschool.ejudge;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ejudge")
@Data
public class EjudgeConfig {

    private String baseUrl;
    private String apiKey;
    private String botLogin;

    /** Таймаут установки соединения. Без него зависший Ejudge держал поток бесконечно. */
    private int connectTimeoutMs = 3000;

    /** Таймаут ожидания ответа. */
    private int readTimeoutMs = 10000;

    /**
     * Идентификатор языка в Ejudge по умолчанию — используется, когда язык
     * задачи не удалось сопоставить. 3 — это Java, ровно то значение, которое
     * раньше было зашито в код.
     */
    private int defaultLangId = 3;

    /**
     * Сопоставление названия языка (в нижнем регистре) с lang_id в Ejudge.
     * Значения зависят от конкретной установки Ejudge, поэтому вынесены в конфиг.
     */
    private Map<String, Integer> languageIds = new LinkedHashMap<>();

    private Polling polling = new Polling();

    @Data
    public static class Polling {
        /** Опрос вердиктов можно выключить: без Ejudge он только сыпал ошибками в лог. */
        private boolean enabled = true;

        /** Период опроса в миллисекундах. */
        private long intervalMs = 3000;
    }

    /**
     * Подбирает lang_id по строке допустимых языков задачи (например "Java, Python").
     * Берётся первый язык, для которого есть сопоставление; иначе — значение по умолчанию.
     */
    public int resolveLangId(String allowedLanguages) {
        if (allowedLanguages == null || allowedLanguages.isBlank() || languageIds.isEmpty()) {
            return defaultLangId;
        }
        for (String raw : allowedLanguages.split(",")) {
            Integer id = languageIds.get(raw.trim().toLowerCase());
            if (id != null) {
                return id;
            }
        }
        return defaultLangId;
    }
}
