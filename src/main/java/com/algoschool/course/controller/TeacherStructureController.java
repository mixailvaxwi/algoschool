package com.algoschool.course.controller;

import com.algoschool.course.dto.TeacherLessonRequest;
import com.algoschool.course.dto.TeacherModuleRequest;
import com.algoschool.course.dto.CourseStructureResponse;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.entity.Module;
import com.algoschool.course.service.CourseStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherStructureController {

    private final CourseStructureService structureService;

    @GetMapping("/courses/{courseId:\\d+}/structure")
    public ResponseEntity<List<CourseStructureResponse>> getCourseStructure(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(structureService.getCourseStructure(courseId, currentUser.getUsername()));
    }

    // Создать модуль внутри курса
    @PostMapping("/courses/{courseId:\\d+}/modules")
    public ResponseEntity<Module> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody TeacherModuleRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Module created = structureService.addModule(courseId, request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Создать урок внутри модуля
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Lesson> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody TeacherLessonRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Lesson created = structureService.addLesson(moduleId, request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Переименовать модуль / изменить его порядок
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<Module> updateModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody TeacherModuleRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(structureService.updateModule(moduleId, request, currentUser.getUsername()));
    }

    // Удалить модуль (вместе со всеми его уроками и шагами)
    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        structureService.deleteModule(moduleId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Переименовать урок / изменить его порядок
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody TeacherLessonRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(structureService.updateLesson(lessonId, request, currentUser.getUsername()));
    }

    // Удалить урок (вместе со всеми его шагами)
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        structureService.deleteLesson(lessonId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
