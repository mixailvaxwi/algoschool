package com.algoschool.submission.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentResult {
    private Long submissionId;
    private String status; // ACCEPTED, WRONG_ANSWER, COMPILATION_ERROR, PENDING
    private String message;
    private String compilerOutput;
    private String testResultsJson;
}