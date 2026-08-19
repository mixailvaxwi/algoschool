package com.algoschool.submission.dto;

import com.algoschool.submission.entity.SubmissionStatus;
import java.time.LocalDateTime;

public record SubmissionHistoryDto(
        Long id,
        String payload,
        SubmissionStatus status,
        LocalDateTime createdAt,
        String compilerOutput,
        String testResultsJson
) {}