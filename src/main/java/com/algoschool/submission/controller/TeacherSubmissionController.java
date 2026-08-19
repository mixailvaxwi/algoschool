package com.algoschool.submission.controller;

import com.algoschool.submission.dto.teacher.TeacherSubmissionDto;
import com.algoschool.submission.entity.SubmissionStatus;
import com.algoschool.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/submissions")
@RequiredArgsConstructor
public class TeacherSubmissionController {

    private final SubmissionService submissionService;

    /**
     * Решения студентов по курсу преподавателя.
     * <p>
     * courseId обязателен: сервис проверяет, что курс принадлежит вызывающему.
     * Без этого эндпоинт отдавал исходный код и ответы всех студентов платформы.
     */
    @GetMapping
    public ResponseEntity<List<TeacherSubmissionDto>> getAllSubmissions(
            @RequestParam Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) SubmissionStatus status,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(
                submissionService.getTeacherSubmissions(courseId, studentId, status, currentUser.getUsername()));
    }
}
