package com.algoschool.grade.controller;

import com.algoschool.grade.dto.MyGradesDto;
import com.algoschool.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Свои оценки по курсу в режиме чтения (UC-S-33). */
@RestController
@RequestMapping("/api/courses/{courseId}")
@RequiredArgsConstructor
public class StudentGradeController {

    private final GradeService gradeService;

    @GetMapping("/my-grades")
    public ResponseEntity<MyGradesDto> myGrades(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(gradeService.myGrades(courseId, currentUser.getUsername()));
    }
}
