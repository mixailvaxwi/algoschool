package com.algoschool.grade.dto;

/**
 * Столбец журнала оценок.
 *
 * @param stepId размещение задачи в уроке; null у ручного элемента
 * @param usageHint подсказка «где стоит задача»: модуль.урок.шаг, null у ручного
 */
public record GradeItemDto(
        Long id,
        String kind,
        Long stepId,
        Long lessonId,
        String title,
        Integer maxScore,
        String policy,
        Integer orderIndex,
        String usageHint
) {}
