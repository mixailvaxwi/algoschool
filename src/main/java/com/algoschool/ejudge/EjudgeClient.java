package com.algoschool.ejudge;

import com.algoschool.ejudge.dto.EjudgeRunStatusResponse;
import com.algoschool.ejudge.dto.EjudgeSubmitResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class EjudgeClient {

    private final EjudgeConfig ejudgeConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Integer submitRun(Integer contestId, String probId, String langId, String sourceCode) {
        String url = ejudgeConfig.getBaseUrl() + "/ej/api/v1/master/submit-run";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(ejudgeConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("contest_id", String.valueOf(contestId));

        body.add("sender_user_login", ejudgeConfig.getBotLogin());

        // УМНАЯ ОТПРАВКА ИДЕНТИФИКАТОРА ЗАДАЧИ
        if (probId.matches("\\d+")) {
            // Если ввели число (например "1"), отправляем как внутренний ID
            body.add("problem", probId);
        } else {
            // Если ввели букву/слово (например "A"), отправляем как имя
            body.add("problem_name", probId);
        }

        body.add("lang_id", langId);
        body.add("file", sourceCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.postForEntity(url, requestEntity, String.class);
            log.info("Сырой ответ Ejudge: {}", rawResponse.getBody());

            EjudgeSubmitResponse response = objectMapper.readValue(rawResponse.getBody(), EjudgeSubmitResponse.class);

            if (response != null && response.isOk()) {
                return response.getResult().getRunId();
            }
            throw new RuntimeException("Ejudge вернул ошибку: " + rawResponse.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("HTTP Ошибка от Ejudge! Статус: {}, Тело: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка авторизации Ejudge", e);
        } catch (Exception e) {
            log.error("Ошибка при отправке кода в Ejudge: {}", e.getMessage());
            throw new RuntimeException("Ошибка связи с Ejudge", e);
        }
    }

    public EjudgeRunStatusResponse getRunStatus(Integer contestId, Integer runId) {
        // 2. ИСПОЛЬЗУЕМ MASTER API ДЛЯ ПРОВЕРКИ СТАТУСА
        String url = ejudgeConfig.getBaseUrl() + "/ej/api/v1/master/run-status-json?contest_id=" + contestId + "&run_id=" + runId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ejudgeConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), String.class);

            log.info("Сырой статус от Ejudge для run_id {}: {}", runId, rawResponse.getBody());

            return objectMapper.readValue(rawResponse.getBody(), EjudgeRunStatusResponse.class);
        } catch (Exception e) {
            log.error("Ошибка проверки статуса в Ejudge: {}", e.getMessage());
            return null;
        }
    }
}