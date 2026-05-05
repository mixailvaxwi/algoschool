package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.TeacherLessonRequest;
import com.algoschool.module_course.dto.TeacherModuleRequest;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.Lesson;
import com.algoschool.module_course.entity.Module;
import com.algoschool.module_course.service.CourseStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherStructureController {

    private final CourseStructureService structureService;

    @Transactional(readOnly = true)
    @GetMapping("/courses/{courseId:\\d+}/structure")
    public ResponseEntity<List<CourseStructureResponse>> getCourseStructure(@PathVariable Long courseId) {
        // Используем тот же сервис, который мы уже починили ранее!
        List<CourseStructureResponse> structure = structureService.getCourseStructure(courseId);
        return ResponseEntity.ok(structure);
    }

    // Создать модуль внутри курса
    @PostMapping("/courses/{courseId:\\d+}/modules")
    public ResponseEntity<Module> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody TeacherModuleRequest request
    ) {
        Module created = structureService.addModule(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Создать урок внутри модуля
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Lesson> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody TeacherLessonRequest request
    ) {
        Lesson created = structureService.addLesson(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}