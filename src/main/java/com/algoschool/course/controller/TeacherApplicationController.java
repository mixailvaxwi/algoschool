package com.algoschool.course.controller;

import com.algoschool.exception.AppException;

import com.algoschool.course.entity.ApplicationStatus;
import com.algoschool.course.service.TeacherApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/courses/{courseId}/applications")
@RequiredArgsConstructor
public class TeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;

    // Метод получения списка (если он у тебя уже есть - просто проверь, что он работает через Principal)
    @GetMapping
    public ResponseEntity<?> getApplications(@PathVariable Long courseId, Principal principal) {
        if (principal == null) throw AppException.unauthorized("Необходима авторизация");
        // Предполагается, что у тебя есть этот метод в сервисе
        return ResponseEntity.ok(teacherApplicationService.getCourseApplications(principal.getName(), courseId));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long courseId,
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        if (principal == null) throw AppException.unauthorized("Необходима авторизация");

        ApplicationStatus newStatus = ApplicationStatus.valueOf(body.get("status"));
        teacherApplicationService.changeApplicationStatus(principal.getName(), courseId, applicationId, newStatus);

        return ResponseEntity.ok(Map.of("message", "Статус заявки обновлен"));
    }
}