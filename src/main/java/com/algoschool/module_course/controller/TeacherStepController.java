package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.TeacherStepRequest;
import com.algoschool.module_course.service.StepService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/teacher/lessons")
@RequiredArgsConstructor
public class TeacherStepController {

    private final StepService stepService; // 👈 Подключаем нужный сервис

    @PostMapping("/{lessonId}/steps")
    public ResponseEntity<Void> addStep(@PathVariable Long lessonId,
                                        @RequestBody TeacherStepRequest request) {

        stepService.addStepToLesson(lessonId, request);
        return ResponseEntity.ok().build();
    }
}