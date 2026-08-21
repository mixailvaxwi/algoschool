package com.algoschool.problem.entity;

import com.algoschool.exception.AppException;

/** Как понимать допуск у числового ответа. */
public enum ToleranceKind {
    /** Отклонение в тех же единицах: |ответ − эталон| ≤ допуск. */
    ABSOLUTE,
    /** Доля от эталона: |ответ − эталон| ≤ допуск · |эталон|. */
    RELATIVE;

    public static ToleranceKind parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return ABSOLUTE;
        }
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Неизвестный вид допуска: " + raw);
        }
    }
}
