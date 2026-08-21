package com.algoschool.problem.dto;

/**
 * Фильтр по банку задач. Все поля необязательны.
 *
 * @param q          подстрока в названии или условии
 * @param type       CHOICE_PROBLEM / INPUT_PROBLEM / CODE_PROBLEM
 * @param difficulty EASY / MEDIUM / HARD
 * @param tag        точное совпадение тега
 * @param onlyMine   true — только свои задачи; иначе свои плюс публичные чужие
 */
public record ProblemSearchQuery(
        String q,
        String type,
        String difficulty,
        String tag,
        boolean onlyMine
) {}
