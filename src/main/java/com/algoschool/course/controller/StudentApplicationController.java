package com.algoschool.course.controller;

import com.algoschool.course.dto.ApplicationResponse;
import com.algoschool.course.service.CourseApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class StudentApplicationController {

    private final CourseApplicationService applicationService;

    /**
     * Заявки текущего студента.
     * <p>
     * Подача заявки раньше жила здесь (POST /courses/{id}/apply) параллельно
     * с записью на курс и по другим правилам. Маршрут удалён: и открытая
     * запись, и заявка на закрытый курс идут через POST /api/courses/{id}/enroll.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(applicationService.getUserApplications(currentUser.getUsername()));
    }
}
