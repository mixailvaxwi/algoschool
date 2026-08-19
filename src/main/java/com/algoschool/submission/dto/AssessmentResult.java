package com.algoschool.submission.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentResult {
    private Long submissionId;
    /** Имя значения SubmissionStatus: CORRECT, WRONG_ANSWER, COMPILATION_ERROR, PENDING... */
    private String status;
    private String message;
    private String compilerOutput;
    private String testResultsJson;
}