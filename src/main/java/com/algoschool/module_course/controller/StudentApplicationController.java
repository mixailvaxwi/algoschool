package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.ApplicationResponse;
import com.algoschool.module_course.repository.CourseApplicationRepository;
import com.algoschool.module_course.service.CourseApplicationService;
import com.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class StudentApplicationController {

    private final CourseApplicationService applicationService;
    private final CourseApplicationRepository applicationRepository;

    // Подать заявку на закрытый курс
    @PostMapping("/courses/{courseId}/apply")
    public ResponseEntity<?> applyToCourse(
            @PathVariable Long courseId,
            @RequestBody Map<String, String> body, // Ожидаем JSON { "message": "Хочу учиться!" }
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        applicationService.applyToCourse(user.getId(), courseId, body.get("message"));
        return ResponseEntity.ok(Map.of("message", "Заявка успешно отправлена"));
    }

    // Посмотреть свои заявки (чтобы знать статус)
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        List<ApplicationResponse> apps = applicationRepository.findAllByStudentId(user.getId())
                .stream()
                .map(applicationService::mapToDto)
                .toList();
        return ResponseEntity.ok(apps);
    }
}