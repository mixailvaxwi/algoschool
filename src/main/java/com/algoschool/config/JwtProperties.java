package com.algoschool.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки подписи JWT.
 * <p>
 * Значений по умолчанию здесь нет намеренно: если секрет не задан, приложение
 * должно упасть на старте, а не молча подписывать токены зашитым в код ключом.
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
@Data
public class JwtProperties {

    /**
     * Секрет в Base64, минимум 256 бит (32 байта) — этого требует HS256.
     * Сгенерировать новый: {@code openssl rand -base64 32}
     */
    @NotBlank(message = "app.jwt.secret не задан — приложению нечем подписывать токены")
    private String secret;

    /** Время жизни токена в миллисекундах. */
    @Positive(message = "app.jwt.expiration должен быть положительным числом миллисекунд")
    private long expiration;
}
