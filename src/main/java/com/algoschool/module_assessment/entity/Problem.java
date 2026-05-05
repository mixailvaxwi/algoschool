package com.mpanyavin.algoschool.module_assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "step_problems")
@Getter
@Setter
@NoArgsConstructor
public abstract class Problem extends Step {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Сложность теперь задается числом (например, от 1 до 100)
    @Column(nullable = false)
    private Integer difficulty;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(name = "attempted_students_count", nullable = false)
    private Integer attemptedStudentsCount = 0;

    @Column(name = "success_students_count", nullable = false)
    private Integer successStudentsCount = 0;
}