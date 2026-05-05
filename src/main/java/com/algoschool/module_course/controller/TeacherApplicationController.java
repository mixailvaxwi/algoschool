package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.ApplicationResponse;
import com.algoschool.module_course.entity.ApplicationStatus;
import com.algoschool.module_course.repository.CourseApplicationRepository;
import com.algoschool.module_course.service.CourseApplicationService;
import com.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')") // Доступ только преподавателям!
@RequiredArgsConstructor
public class TeacherApplicationController {

    private final CourseApplicationService applicationService;
    private final CourseApplicationRepository applicationRepository;

    // Получить список ожидающих заявок на курс
    @GetMapping("/courses/{courseId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getPendingApplications(
            @PathVariable Long courseId
    ) {
        // Запрашиваем только те, что ждут решения (PENDING)
        List<ApplicationResponse> apps = applicationRepository
                .findAllByCourseIdAndStatus(courseId, ApplicationStatus.PENDING)
                .stream()
                .map(applicationService::mapToDto)
                .toList();
        return ResponseEntity.ok(apps);
    }

    // Одобрить заявку
    @PostMapping("/applications/{appId}/approve")
    public ResponseEntity<?> approveApplication(
            @PathVariable Long appId,
            @AuthenticationPrincipal UserDetailsImpl teacher
    ) {
        applicationService.reviewApplication(teacher.getId(), appId, true);
        return ResponseEntity.ok(Map.of("message", "Студент успешно зачислен на курс"));
    }

    // Отклонить заявку
    @PostMapping("/applications/{appId}/reject")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long appId,
            @AuthenticationPrincipal UserDetailsImpl teacher
    ) {
        applicationService.reviewApplication(teacher.getId(), appId, false);
        return ResponseEntity.ok(Map.of("message", "Заявка отклонена"));
    }
}