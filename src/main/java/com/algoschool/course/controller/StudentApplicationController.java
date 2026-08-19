package com.algoschool.course.controller;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.ApplicationResponse;
import com.algoschool.course.service.CourseApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class StudentApplicationController {

    private final CourseApplicationService applicationService;

    @PostMapping("/courses/{courseId}/apply")
    public ResponseEntity<?> applyToCourse(
            @PathVariable Long courseId,
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        if (principal == null) {
            throw AppException.unauthorized("Для подачи заявки необходимо авторизоваться");
        }
        applicationService.applyToCourse(principal.getName(), courseId, body.get("message"));
        return ResponseEntity.ok(Map.of("message", "Заявка успешно отправлена"));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Principal principal) {
        if (principal == null) {
            throw AppException.unauthorized("Пользователь не авторизован");
        }
        return ResponseEntity.ok(applicationService.getUserApplications(principal.getName()));
    }
}