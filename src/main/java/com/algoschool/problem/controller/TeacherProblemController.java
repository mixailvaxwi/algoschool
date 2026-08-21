package com.algoschool.problem.controller;

import com.algoschool.problem.dto.ProblemDto;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.dto.ProblemSearchQuery;
import com.algoschool.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Банк задач преподавателя (UC-T-20…22).
 * <p>
 * Маршрут внутри /api/teacher/**, поэтому доступ ограничен ролью
 * преподавателя на уровне SecurityConfig; принадлежность конкретной задачи
 * проверяет ProblemService.
 */
@RestController
@RequestMapping("/api/teacher/problems")
@RequiredArgsConstructor
public class TeacherProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "false") boolean onlyMine,
            @AuthenticationPrincipal UserDetails currentUser) {

        ProblemSearchQuery query = new ProblemSearchQuery(q, type, difficulty, tag, onlyMine);
        return ResponseEntity.ok(problemService.search(query, currentUser.getUsername()));
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> tags(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(problemService.tags(currentUser.getUsername()));
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ProblemDto> get(
            @PathVariable Long problemId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(problemService.get(problemId, currentUser.getUsername()));
    }

    @PostMapping
    public ResponseEntity<ProblemDto> create(
            @Valid @RequestBody ProblemRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(problemService.create(request, currentUser.getUsername()));
    }

    @PutMapping("/{problemId}")
    public ResponseEntity<ProblemDto> update(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(problemService.update(problemId, request, currentUser.getUsername()));
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long problemId,
            @AuthenticationPrincipal UserDetails currentUser) {

        problemService.delete(problemId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
