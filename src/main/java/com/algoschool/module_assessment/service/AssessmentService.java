package com.mpanyavin.algoschool.module_assessment.service;

import com.mpanyavin.algoschool.module_assessment.dto.SubmissionRequest;
import com.mpanyavin.algoschool.module_assessment.dto.SubmissionResponse;

public interface AssessmentService {
    SubmissionResponse submitSolution(Long userId, SubmissionRequest request);
}