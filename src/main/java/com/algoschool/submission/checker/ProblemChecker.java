package com.algoschool.submission.checker;

import com.algoschool.problem.entity.Problem;
import com.algoschool.submission.entity.SubmissionStatus;

/**
 * Проверяющий модуль конкретного типа задачи.
 * <p>
 * Принимает задачу из банка, а не шаг урока: правильный ответ принадлежит
 * задаче и одинаков во всех уроках, где она стоит.
 */
public interface ProblemChecker {
    boolean supports(Problem problem);

    /** Конкретный статус вместо true/false: PENDING — тоже нормальный исход. */
    SubmissionStatus check(Problem problem, String payload);
}
