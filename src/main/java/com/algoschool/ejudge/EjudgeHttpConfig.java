package com.algoschool.ejudge;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP-клиент для Ejudge.
 * <p>
 * Раньше {@code RestTemplate} создавался прямо в поле сервиса и не имел
 * таймаутов: недоступный Ejudge держал поток планировщика (а вместе с ним и
 * открытую транзакцию) неограниченно долго.
 */
@Configuration
@RequiredArgsConstructor
public class EjudgeHttpConfig {

    private final EjudgeConfig ejudgeConfig;

    @Bean
    public RestTemplate ejudgeRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(ejudgeConfig.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(ejudgeConfig.getReadTimeoutMs()));
        return new RestTemplate(factory);
    }
}
