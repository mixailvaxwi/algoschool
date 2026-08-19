package com.algoschool.submission.controller;

import com.algoschool.submission.service.StepProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons/{lessonId}/steps/{stepId}")
@RequiredArgsConstructor
public class StudentProgressController {

    private final StepProgressService progressService;

    // Эндпоинт для отметки теоретического шага как прочитанного
    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable Long stepId,
            @AuthenticationPrincipal UserDetails currentUser) {

        progressService.markTheoryAsCompleted(stepId, currentUser.getUsername());

        // Возвращаем просто 200 OK без тела, так как фронтенду достаточно знать, что запрос прошел успешно
        return ResponseEntity.ok().build();
    }
}