package com.algoschool.grade.entity;

import com.algoschool.exception.AppException;

import java.util.List;
import java.util.OptionalDouble;

/**
 * Как из многих попыток получается один зачтённый балл.
 * <p>
 * Правило задаёт преподаватель на уровне элемента: где-то важно, что студент
 * в итоге разобрался ({@link #BEST}), где-то — что он сдал с первого раза
 * ({@link #FIRST}).
 * <p>
 * Считает не в баллах, а в долях от максимума попытки. Вес элемента в курсе и
 * вес задачи в банке — разные величины, и вес элемента преподаватель может
 * менять уже после того, как студенты нарешали попыток; доля переживает такую
 * смену без пересчёта истории.
 */
public enum GradePolicy {
    /** Лучшая попытка. По умолчанию: повторная отправка не наказывается. */
    BEST,
    /** Последняя попытка: засчитывается то, что студент сдал в итоге. */
    LAST,
    /** Первая попытка. */
    FIRST,
    /** Среднее по попыткам. */
    AVERAGE;

    /**
     * @param fractions доли от максимума по проверенным попыткам, от старой к
     *                  новой; непроверенные (PENDING, сбой отправки) сюда не
     *                  попадают
     * @return зачтённая доля или пусто, если засчитывать нечего
     */
    public OptionalDouble apply(List<Double> fractions) {
        if (fractions.isEmpty()) {
            return OptionalDouble.empty();
        }
        return switch (this) {
            case BEST -> fractions.stream().mapToDouble(Double::doubleValue).max();
            case LAST -> OptionalDouble.of(fractions.get(fractions.size() - 1));
            case FIRST -> OptionalDouble.of(fractions.get(0));
            case AVERAGE -> fractions.stream().mapToDouble(Double::doubleValue).average();
        };
    }

    public static GradePolicy parse(String raw) {
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw AppException.badRequest("Неизвестная политика зачёта: " + raw);
        }
    }
}
