package com.mpanyavin.algoschool.module_assessment.entity;

import com.mpanyavin.algoschool.module_user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_problem_successes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProblemSuccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // Сколько опыта было реально начислено (на случай, если xpReward у задачи позже изменят)
    @Column(name = "earned_xp", nullable = false)
    private Integer earnedXp;

    @Column(name = "solved_at", nullable = false)
    @Builder.Default
    private LocalDateTime solvedAt = LocalDateTime.now();
}