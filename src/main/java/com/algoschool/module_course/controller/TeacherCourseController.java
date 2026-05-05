package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.TeacherCourseRequest;
import com.algoschool.module_course.entity.Course;
import com.algoschool.module_course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    // Получить ВСЕ курсы (включая черновики) для таблицы
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCoursesForAdmin());
    }

    // Создать новый курс (наша форма)
    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody TeacherCourseRequest request) {
        Course created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Обновить курс
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody TeacherCourseRequest request
    ) {
        Course updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updated);
    }

    // Удалить курс
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}