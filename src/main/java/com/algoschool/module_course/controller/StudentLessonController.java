package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.LessonPlayerResponse;
import com.algoschool.module_course.service.LessonService;
import com.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class StudentLessonController {

    private final LessonService lessonService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonPlayerResponse> getFullLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        LessonPlayerResponse response = lessonService.getFullLesson(lessonId, currentUser);
        return ResponseEntity.ok(response);
    }
}