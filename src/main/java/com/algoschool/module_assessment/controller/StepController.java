package com.mpanyavin.algoschool.module_assessment.controller;

import com.mpanyavin.algoschool.module_assessment.entity.Step;
import com.mpanyavin.algoschool.module_assessment.repository.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class StepController {

    private final StepRepository stepRepository;

    // Метод: GET /api/lessons/{lessonId}/steps
    // Эндпоинт можно оставить открытым или закрыть только для тех, кто купил курс
    @GetMapping("/{lessonId}/steps")
    public ResponseEntity<List<Step>> getLessonSteps(@PathVariable Long lessonId) {
        // Достаем все шаги урока и сортируем их по порядку
        List<Step> steps = stepRepository.findAllByLessonId(
                lessonId,
                Sort.by(Sort.Direction.ASC, "orderIndex")
        );
        return ResponseEntity.ok(steps);
    }
}