package com.algoschool.submission.checker;

import com.algoschool.problem.entity.Problem;

/**
 * Проверяющий модуль конкретного типа задачи.
 * <p>
 * Принимает задачу из банка, а не шаг урока: правильный ответ принадлежит
 * задаче и одинаков во всех уроках, где она стоит.
 */
public interface ProblemChecker {
    boolean supports(Problem problem);

    /** Вердикт и балл: не всякий ответ бывает только верным или только неверным. */
    CheckResult check(Problem problem, String payload);
}
