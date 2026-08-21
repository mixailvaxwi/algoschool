package com.algoschool.submission.entity;

import com.algoschool.problem.entity.Problem;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.persistence.Column;

@Entity
@Table(name = "submissions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Что решали. История попыток и счётчики принадлежат задаче, а не уроку. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    /**
     * Где решали: задача может стоять в нескольких уроках, и отметку
     * «пройдено» надо ставить именно тому шагу, в котором студент был.
     * <p>
     * Null, если размещение потом сняли с урока — задача и само решение при
     * этом остаются (внешний ключ объявлен ON DELETE SET NULL).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private ProblemStep step;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "external_run_id")
    private Integer externalRunId;

    @Column(name = "compiler_output", columnDefinition = "TEXT")
    private String compilerOutput;

    @Column(name = "test_results_json", columnDefinition = "TEXT")
    private String testResultsJson;
}