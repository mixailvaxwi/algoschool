package com.algoschool.course.controller;

import com.algoschool.course.dto.teacher.ApplicationDto;
import com.algoschool.course.dto.teacher.ApplicationStatusUpdateRequest;
import com.algoschool.course.service.TeacherApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/courses/{courseId}/applications")
@RequiredArgsConstructor
public class TeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;

    @GetMapping
    public ResponseEntity<List<ApplicationDto>> getApplications(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(
                teacherApplicationService.getCourseApplications(currentUser.getUsername(), courseId));
    }

    /**
     * Смена статуса заявки.
     * <p>
     * Раньше тело читалось как {@code Map<String, String>}, а статус получался
     * через {@code ApplicationStatus.valueOf(body.get("status"))}: отсутствующее
     * поле давало NPE, а незнакомое значение — IllegalArgumentException, и то и
     * другое уходило наружу как 500. Типизированный DTO с @NotNull отвечает 400.
     */
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable Long courseId,
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        teacherApplicationService.changeApplicationStatus(
                currentUser.getUsername(), courseId, applicationId, request.getStatus());

        return ResponseEntity.ok(Map.of("message", "Статус заявки обновлен"));
    }
}
