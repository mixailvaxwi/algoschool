package com.algoschool.course.controller;

import com.algoschool.course.dto.TeacherCourseRequest;
import com.algoschool.course.dto.teacher.CourseDto;
import com.algoschool.course.service.TeacherCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    // Только сервис! Никаких репозиториев здесь быть не должно.
    private final TeacherCourseService teacherCourseService;

    // Получить все курсы преподавателя
    @GetMapping
    public ResponseEntity<List<CourseDto>> getMyCourses(@AuthenticationPrincipal UserDetails currentUser) {
        // Вызываем правильный метод из нового сервиса
        return ResponseEntity.ok(teacherCourseService.getAllCoursesForTeacher(currentUser.getUsername()));
    }

    // Создать новый курс (черновик)
    @PostMapping
    public ResponseEntity<CourseDto> createCourse(
            @RequestBody TeacherCourseRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(teacherCourseService.createCourse(request, currentUser.getUsername()));
    }

    // Обновить данные курса
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable Long courseId,
            @RequestBody TeacherCourseRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(teacherCourseService.updateCourse(courseId, request, currentUser.getUsername()));
    }

    // НОВЫЙ ЭНДПОИНТ: Опубликовать / Скрыть курс
    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<Void> togglePublish(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {
        teacherCourseService.togglePublishStatus(courseId, currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    // Удалить курс
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {
        teacherCourseService.deleteCourse(courseId, currentUser.getUsername());
        return ResponseEntity.noContent().build(); // 204 No Content - стандарт для удаления
    }
}