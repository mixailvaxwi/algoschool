package com.algoschool.ejudge;

import com.algoschool.ejudge.dto.EjudgeRunStatusResponse;
import com.algoschool.ejudge.dto.EjudgeSubmitResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
public class EjudgeClient {

    private final EjudgeConfig ejudgeConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EjudgeClient(EjudgeConfig ejudgeConfig,
                        @Qualifier("ejudgeRestTemplate") RestTemplate restTemplate,
                        ObjectMapper objectMapper) {
        this.ejudgeConfig = ejudgeConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Integer submitRun(Integer contestId, String probId, String allowedLanguages, String sourceCode) {
        String url = ejudgeConfig.getBaseUrl() + "/ej/api/v1/master/submit-run";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(ejudgeConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("contest_id", String.valueOf(contestId));
        body.add("sender_user_login", ejudgeConfig.getBotLogin());

        // Число трактуем как внутренний id задачи, слово — как её имя
        if (probId != null && probId.matches("\\d+")) {
            body.add("problem", probId);
        } else {
            body.add("problem_name", probId);
        }

        // lang_id больше не зашит в код: подбирается по языкам задачи (см. ejudge.language-ids)
        body.add("lang_id", String.valueOf(ejudgeConfig.resolveLangId(allowedLanguages)));
        body.add("file", sourceCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.postForEntity(url, requestEntity, String.class);
            EjudgeSubmitResponse response = objectMapper.readValue(rawResponse.getBody(), EjudgeSubmitResponse.class);

            if (response != null && response.isOk() && response.getResult() != null) {
                return response.getResult().getRunId();
            }
            throw new EjudgeUnavailableException("Ejudge вернул ошибку: " + rawResponse.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("Ejudge ответил {} на отправку решения: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new EjudgeUnavailableException("Ejudge отклонил запрос: " + e.getStatusCode(), e);
        } catch (EjudgeUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Не удалось связаться с Ejudge: {}", e.getMessage());
            throw new EjudgeUnavailableException("Нет связи с проверяющей системой", e);
        }
    }

    /**
     * @return ответ Ejudge, либо {@code null}, если связаться не удалось —
     *         вызывающий просто попробует на следующем цикле опроса.
     */
    public EjudgeRunStatusResponse getRunStatus(Integer contestId, Integer runId) {
        String url = ejudgeConfig.getBaseUrl()
                + "/ej/api/v1/master/run-status-json?contest_id=" + contestId + "&run_id=" + runId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ejudgeConfig.getApiKey());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return objectMapper.readValue(rawResponse.getBody(), EjudgeRunStatusResponse.class);
        } catch (Exception e) {
            log.warn("Не удалось получить статус run_id={} из Ejudge: {}", runId, e.getMessage());
            return null;
        }
    }
}
