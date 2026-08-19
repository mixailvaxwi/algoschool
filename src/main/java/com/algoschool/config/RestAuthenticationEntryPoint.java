package com.algoschool.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ответ на запрос без валидной аутентификации.
 * <p>
 * По умолчанию Spring Security отдаёт здесь 403, из-за чего фронтенд не мог
 * отличить «токен протух, надо перелогиниться» от «прав не хватает».
 * Теперь это честный 401.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorWriter errorWriter;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorWriter.write(response, HttpStatus.UNAUTHORIZED, "Требуется вход в систему");
    }
}
