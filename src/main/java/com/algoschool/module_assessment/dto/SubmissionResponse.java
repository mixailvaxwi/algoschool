package com.mpanyavin.algoschool.module_assessment.dto;

public record SubmissionResponse(
        Long submissionId,
        String status,
        Integer xpEarned // Сколько опыта было получено за эту конкретную попытку (0, если задача уже была решена ранее)
) {}