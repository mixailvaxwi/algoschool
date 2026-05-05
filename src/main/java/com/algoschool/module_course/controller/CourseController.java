package com.mpanyavin.algoschool.module_course.controller;

import com.mpanyavin.algoschool.module_course.dto.CoursePurchaseResponse;
import com.mpanyavin.algoschool.module_course.service.CourseService;
import com.mpanyavin.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Метод: POST /api/courses/{courseId}/purchase
    @PostMapping("/{courseId}/purchase")
    public ResponseEntity<CoursePurchaseResponse> purchaseCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CoursePurchaseResponse response = courseService.purchaseCourse(
                userDetails.getId(),
                courseId
        );

        return ResponseEntity.ok(response);
    }
}