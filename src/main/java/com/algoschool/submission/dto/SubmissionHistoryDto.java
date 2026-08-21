package com.algoschool.submission.dto;

import com.algoschool.submission.entity.SubmissionStatus;
import java.time.LocalDateTime;

/**
 * Одна попытка в истории студента.
 *
 * @param score         балл за попытку; null — проверка ещё не закончена
 * @param reviewComment что написал проверяющий: без объяснения балл за
 *                      развёрнутый ответ выглядит произволом
 */
public record SubmissionHistoryDto(
        Long id,
        String payload,
        SubmissionStatus status,
        LocalDateTime createdAt,
        String compilerOutput,
        String testResultsJson,
        Integer score,
        Integer maxScore,
        String reviewComment,
        String reviewedBy
) {}