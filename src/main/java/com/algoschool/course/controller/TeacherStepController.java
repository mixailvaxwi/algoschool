package com.algoschool.course.controller;

import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.entity.Step;
import com.algoschool.step.service.StepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/courses/{courseId}/lessons/{lessonId}/steps")
@RequiredArgsConstructor
public class TeacherStepController {

    private final StepService stepService;

    @PostMapping
    public ResponseEntity<Step> createStep(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @Valid @RequestBody StepCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        Step createdStep = stepService.addStepToLesson(lessonId, request, currentUser.getUsername());
        return ResponseEntity.ok(createdStep);
    }
}
