package com.algoschool.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Бизнес-исключение с явным HTTP-статусом.
 * <p>
 * Бросайте его вместо голого {@link RuntimeException}: иначе ошибка доедет до
 * клиента как 500 «внутренняя ошибка сервера», и фронтенд не сможет отличить
 * «курс не найден» от «база упала».
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /** 404 — сущности нет. */
    public static AppException notFound(String message) {
        return new AppException(HttpStatus.NOT_FOUND, message);
    }

    /** 409 — состояние конфликтует с запросом (уже записан, логин занят). */
    public static AppException conflict(String message) {
        return new AppException(HttpStatus.CONFLICT, message);
    }

    /** 403 — пользователь известен, но прав на объект нет. */
    public static AppException forbidden(String message) {
        return new AppException(HttpStatus.FORBIDDEN, message);
    }

    /** 400 — запрос некорректен по существу. */
    public static AppException badRequest(String message) {
        return new AppException(HttpStatus.BAD_REQUEST, message);
    }

    /** 401 — требуется вход. */
    public static AppException unauthorized(String message) {
        return new AppException(HttpStatus.UNAUTHORIZED, message);
    }
}
