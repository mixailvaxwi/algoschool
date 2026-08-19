package com.algoschool.submission.dto;

import lombok.Data;
import java.util.List;

@Data
public class StepCreateRequest {
    private String stepType;

    private Integer orderIndex;

    private String content;

    private String description;

    private List<String> options;
    private Integer correctOptionIndex;
    private Boolean isMultipleChoice;

    private String correctAnswer;

    private Integer timeLimitSec;
    private Integer memoryLimitMb;
    private String allowedLanguages;

    private Integer ejudgeContestId;
    private String ejudgeProblemId;
}