// Problem.java (АБСТРАКТНАЯ ЗАДАЧА)
package com.algoschool.step.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "step_problems")
@Getter @Setter
public abstract class Problem extends Step {
    // Description теперь хранится только тут!
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "attempted_students_count", nullable = false)
    private Integer attemptedStudentsCount = 0;

    @Column(name = "success_students_count", nullable = false)
    private Integer successStudentsCount = 0;
}