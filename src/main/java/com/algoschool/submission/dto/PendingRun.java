package com.algoschool.submission.dto;

/**
 * Минимум данных о решении, ожидающем вердикта.
 * <p>
 * Опрос ходит по сети вне транзакции, поэтому сущности туда отдавать нельзя:
 * они были бы отсоединены, и любое обращение к ленивой связи упало бы.
 */
public record PendingRun(
        Long submissionId,
        Integer contestId,
        Integer externalRunId
) {}
