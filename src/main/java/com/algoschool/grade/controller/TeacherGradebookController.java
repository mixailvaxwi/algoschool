package com.algoschool.grade.controller;

import com.algoschool.grade.dto.GradeItemDto;
import com.algoschool.grade.dto.GradeItemRequest;
import com.algoschool.grade.dto.GradeUpdateRequest;
import com.algoschool.grade.dto.GradebookDto;
import com.algoschool.grade.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Журнал оценок курса (UC-T-43…45). */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherGradebookController {

    private final GradeService gradeService;

    @GetMapping("/courses/{courseId}/gradebook")
    public ResponseEntity<GradebookDto> gradebook(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(gradeService.gradebook(courseId, currentUser.getUsername()));
    }

    @GetMapping("/courses/{courseId}/gradebook/export")
    public ResponseEntity<Resource> exportGradebook(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {

        byte[] csv = gradeService.exportGradebookCsv(courseId, currentUser.getUsername())
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("gradebook-" + courseId + ".csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(new ByteArrayResource(csv));
    }

    @GetMapping("/courses/{courseId}/grade-items")
    public ResponseEntity<List<GradeItemDto>> items(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(gradeService.items(courseId, currentUser.getUsername()));
    }

    /** Ручной столбец журнала: экзамен, проект, работа на семинаре. */
    @PostMapping("/courses/{courseId}/grade-items")
    public ResponseEntity<GradeItemDto> createManualItem(
            @PathVariable Long courseId,
            @Valid @RequestBody GradeItemRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(gradeService.createManualItem(courseId, request, currentUser.getUsername()));
    }

    @PutMapping("/grade-items/{itemId}")
    public ResponseEntity<GradeItemDto> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody GradeItemRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(gradeService.updateItem(itemId, request, currentUser.getUsername()));
    }

    @DeleteMapping("/grade-items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails currentUser) {

        gradeService.deleteItem(itemId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/grades")
    public ResponseEntity<Void> setGrade(
            @Valid @RequestBody GradeUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        gradeService.setManualGrade(request, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    /** Снять ручную оценку: клетка снова считается по решениям студента. */
    @DeleteMapping("/grades/{itemId}/{userId}")
    public ResponseEntity<Void> clearGrade(
            @PathVariable Long itemId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {

        gradeService.clearManualGrade(itemId, userId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
