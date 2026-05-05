package com.algoschool.module_assessment.controller;

import com.algoschool.module_assessment.dto.AssessmentResult;
import com.algoschool.module_assessment.dto.SubmissionRequest;
import com.algoschool.module_assessment.service.AssessmentService;
import com.algoschool.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons/{lessonId}/steps/{stepId}/submit")
@RequiredArgsConstructor
public class SubmissionController {

    private final AssessmentService assessmentService;

    @PostMapping
    public ResponseEntity<AssessmentResult> submit(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable Long stepId,
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        Long currentUserId = currentUser.getId();

        AssessmentResult result = assessmentService.submitSolution(
                currentUserId,
                stepId,
                request.answer()
        );

        return ResponseEntity.ok(result);
    }
}