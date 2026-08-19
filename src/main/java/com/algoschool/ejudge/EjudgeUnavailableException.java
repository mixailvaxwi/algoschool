package com.algoschool.ejudge;

/**
 * Не удалось передать решение проверяющей системе.
 * <p>
 * Это сбой инфраструктуры, а не вердикт по решению студента: раньше такой
 * случай записывался как RUNTIME_ERROR и выглядел как ошибка в коде студента.
 */
public class EjudgeUnavailableException extends RuntimeException {

    public EjudgeUnavailableException(String message) {
        super(message);
    }

    public EjudgeUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
