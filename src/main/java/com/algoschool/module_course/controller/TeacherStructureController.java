package com.algoschool.module_course.controller;

import com.algoschool.module_course.dto.AdminLessonRequest;
import com.algoschool.module_course.dto.AdminModuleRequest;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.Lesson;
import com.algoschool.module_course.entity.Module;
import com.algoschool.module_course.service.CourseStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStructureController {

    private final CourseStructureService structureService;

    // Получить всё дерево модулей и уроков курса
    @GetMapping("/courses/{courseId}/structure")
    public ResponseEntity<List<CourseStructureResponse>> getStructure(@PathVariable Long courseId) {
        return ResponseEntity.ok(structureService.getCourseStructure(courseId));
    }

    // Создать модуль внутри курса
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<Module> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody AdminModuleRequest request
    ) {
        Module created = structureService.addModule(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Создать урок внутри модуля
    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Lesson> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody AdminLessonRequest request
    ) {
        Lesson created = structureService.addLesson(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}