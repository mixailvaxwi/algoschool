package com.algoschool.ejudge;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ejudge")
@Data
public class EjudgeConfig {
    private String baseUrl;
    private String apiKey;
    private String botLogin;
}