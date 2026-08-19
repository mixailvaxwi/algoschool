package com.algoschool.submission.controller;

import com.algoschool.submission.dto.AssessmentResult; // ИСПРАВЛЕН ИМПОРТ
import com.algoschool.submission.dto.SubmissionRequest;
import com.algoschool.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.algoschool.submission.dto.SubmissionHistoryDto;
import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons/{lessonId}/steps/{stepId}/submit")
@RequiredArgsConstructor
public class StudentSubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<AssessmentResult> submitSolution( // ИЗМЕНЕН ТИП ОТВЕТА
                                                            @PathVariable Long courseId,
                                                            @PathVariable Long lessonId,
                                                            @PathVariable Long stepId,
                                                            @Valid @RequestBody SubmissionRequest request,
                                                            @AuthenticationPrincipal UserDetails currentUser) {

        AssessmentResult response = submissionService.processSubmission(stepId, request, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SubmissionHistoryDto>> getHistory(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable Long stepId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(submissionService.getStudentHistory(stepId, currentUser.getUsername()));
    }
}