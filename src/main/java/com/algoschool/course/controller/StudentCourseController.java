package com.algoschool.course.controller;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.CourseInfoResponse;
import com.algoschool.course.dto.CourseStructureResponse;
import com.algoschool.course.dto.student.CourseCatalogDto;
import com.algoschool.course.dto.student.EnrollmentRequest;
import com.algoschool.course.dto.student.EnrollmentResponse;
import com.algoschool.course.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // НОВЫЙ БЕЗОПАСНЫЙ ИМПОРТ
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    @GetMapping
    public ResponseEntity<List<CourseCatalogDto>> getCatalog(Principal principal) {
        // Если авторизован - передаем логин, если нет - null
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(studentCourseService.getCatalog(username));
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseCatalogDto>> getEnrolledCourses(Principal principal) {
        if (principal == null) throw AppException.unauthorized("Авторизуйтесь, чтобы увидеть свои курсы");
        return ResponseEntity.ok(studentCourseService.getEnrolledCourses(principal.getName()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseInfoResponse> getCourseInfo(
            @PathVariable Long courseId,
            Principal principal) {
        String username = (principal != null) ? principal.getName() : "anonymousUser";
        return ResponseEntity.ok(studentCourseService.getCourseInfo(courseId, username));
    }

    @GetMapping("/{courseId}/structure")
    public ResponseEntity<List<CourseStructureResponse>> getStructure(
            @PathVariable Long courseId,
            Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(studentCourseService.getCourseStructure(courseId, username));
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(
            @PathVariable Long courseId,
            @RequestBody(required = false) EnrollmentRequest request,
            Principal principal) {

        // Никаких instanceof! Если principal == null, значит токена нет.
        if (principal == null) {
            throw AppException.unauthorized("Для записи на курс необходимо войти в аккаунт");
        }

        return ResponseEntity.ok(studentCourseService.enrollInCourse(principal.getName(), courseId, request));
    }
}