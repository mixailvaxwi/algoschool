package com.mpanyavin.algoschool.module_assessment.controller;

import com.mpanyavin.algoschool.module_assessment.dto.SubmissionRequest;
import com.mpanyavin.algoschool.module_assessment.dto.SubmissionResponse;
import com.mpanyavin.algoschool.module_assessment.service.AssessmentService;
import com.mpanyavin.algoschool.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final AssessmentService assessmentService;

    // Метод: POST /api/submissions
    // Требует наличие заголовка Authorization: Bearer <token>
    @PostMapping
    public ResponseEntity<SubmissionResponse> submitSolution(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SubmissionRequest request
    ) {
        // Передаем ID пользователя из токена и само решение в сервисный слой
        SubmissionResponse response = assessmentService.submitSolution(userDetails.getId(), request);

        return ResponseEntity.ok(response);
    }
}