package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.TeacherCourseRequest;
import com.algoschool.module_course.entity.Course;
import com.algoschool.module_course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/courses") // <-- Тот самый путь, который не мог найти фронтенд!
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseService courseService;

    // 1. Получить все курсы текущего преподавателя
    @GetMapping
    public ResponseEntity<List<Course>> getMyCourses(Authentication authentication) {
        // authentication.getName() обычно возвращает username или email текущего юзера
        return ResponseEntity.ok(courseService.getAllCoursesForTeacher(authentication.getName()));
    }

    // 2. Создать новый курс
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody TeacherCourseRequest request,
                                               Authentication authentication) {
        Course createdCourse = courseService.createCourse(request, authentication.getName());
        return ResponseEntity.ok(createdCourse);
    }

    // 3. Удалить курс
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id,
                                             Authentication authentication) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}