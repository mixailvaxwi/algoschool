package com.algoschool.course.controller;

import com.algoschool.course.dto.player.LessonPlayerResponse; // НОВЫЙ ИМПОРТ
import com.algoschool.course.service.StudentLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // НОВЫЙ ИМПОРТ
import org.springframework.security.core.userdetails.UserDetails; // НОВЫЙ ИМПОРТ
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class StudentLessonController {

    private final StudentLessonService studentLessonService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonPlayerResponse> getLessonPlayer(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails currentUser) { // ДОБАВЛЕНО

        // Теперь передаем username в сервис для получения прогресса
        LessonPlayerResponse response = studentLessonService.getLessonForPlayer(courseId, lessonId, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }
}