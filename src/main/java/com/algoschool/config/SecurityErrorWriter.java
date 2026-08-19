package com.algoschool.config;

import com.algoschool.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Пишет тот же JSON, что и {@link com.algoschool.exception.GlobalExceptionHandler}.
 * <p>
 * Нужен потому, что фильтры и точки входа Spring Security работают вне
 * DispatcherServlet — {@code @RestControllerAdvice} до них не достаёт, и без
 * этого клиент получал бы HTML-страницу ошибки контейнера вместо JSON.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
