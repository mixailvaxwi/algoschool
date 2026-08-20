package com.algoschool.course.controller;

import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.dto.StepTeacherDto;
import com.algoschool.step.service.StepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/courses/{courseId}/lessons/{lessonId}/steps")
@RequiredArgsConstructor
public class TeacherStepController {

    private final StepService stepService;

    @GetMapping
    public ResponseEntity<List<StepTeacherDto>> getSteps(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(stepService.getStepsForLesson(lessonId, currentUser.getUsername()));
    }

    @PostMapping
    public ResponseEntity<StepTeacherDto> createStep(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @Valid @RequestBody StepCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        StepTeacherDto createdStep = stepService.addStepToLesson(lessonId, request, currentUser.getUsername());
        return ResponseEntity.ok(createdStep);
    }

    @PutMapping("/{stepId}")
    public ResponseEntity<StepTeacherDto> updateStep(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable Long stepId,
            @Valid @RequestBody StepCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(stepService.updateStep(stepId, request, currentUser.getUsername()));
    }

    @DeleteMapping("/{stepId}")
    public ResponseEntity<Void> deleteStep(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable Long stepId,
            @AuthenticationPrincipal UserDetails currentUser) {

        stepService.deleteStep(stepId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
