package com.algoschool.submission.controller;

import com.algoschool.submission.dto.teacher.ReviewItemDto;
import com.algoschool.submission.dto.teacher.ReviewRequest;
import com.algoschool.submission.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Очередь ручной проверки развёрнутых ответов (UC-T-40). */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherReviewController {

    private final ReviewService reviewService;

    /**
     * courseId обязателен: развёрнутый ответ видит только проверяющий, и без
     * привязки к курсу очередь отдавала бы чужие ответы любому преподавателю.
     */
    @GetMapping("/review-queue")
    public ResponseEntity<List<ReviewItemDto>> queue(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(reviewService.queue(courseId, currentUser.getUsername()));
    }

    @PostMapping("/submissions/{submissionId}/review")
    public ResponseEntity<Void> review(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        reviewService.review(submissionId, request, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
