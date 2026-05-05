package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.CourseCatalogResponse;
import com.algoschool.module_course.dto.CourseInfoResponse;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.AccessType;
import com.algoschool.module_course.entity.Course;
import com.algoschool.module_course.entity.UserCourse;
import com.algoschool.module_course.repository.CourseRepository;
import com.algoschool.module_course.repository.UserCourseRepository;
import com.algoschool.module_course.service.CourseService;
import com.algoschool.module_user.entity.User;
import com.algoschool.module_user.repository.UserRepository;
import com.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    @GetMapping
    public ResponseEntity<List<CourseCatalogResponse>> getCatalog(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        List<CourseCatalogResponse> catalog = courseService.getCatalog(userId);
        return ResponseEntity.ok(catalog);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<CourseInfoResponse> getCourseById(@PathVariable Long id, Authentication authentication) {
        // Достаем имя пользователя, если он авторизован
        String username = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : null;

        return ResponseEntity.ok(courseService.getCourseInfo(id, username));
    }

    @GetMapping("/{courseId:\\d+}/structure")
    public ResponseEntity<List<CourseStructureResponse>> getCourseStructure(@PathVariable Long courseId) {
        List<CourseStructureResponse> structure = courseService.getCourseStructure(courseId);
        return ResponseEntity.ok(structure);
    }

    @PostMapping("/{courseId:\\d+}/enroll")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId, Authentication authentication) {
        // authentication.getName() вернет username или email текущего студента
        courseService.enrollInCourse(authentication.getName(), courseId);
        return ResponseEntity.ok().build();
    }
}