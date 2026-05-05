package com.algoschool.module_course.controller;

import com.algoschool.module_assessment.entity.Step;
import com.algoschool.module_course.service.StepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/lessons/{lessonId}/steps")
@RequiredArgsConstructor
public class AdminStepController {

    private final StepService stepService;

    // Эндпоинт ждет JSON с полем "stepType"
    @PostMapping
    public ResponseEntity<Step> addStep(
            @PathVariable Long lessonId,
            @RequestBody Step step // Jackson автоматически создаст нужный класс-наследник!
    ) {
        Step createdStep = stepService.addStepToLesson(lessonId, step);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStep);
    }
}