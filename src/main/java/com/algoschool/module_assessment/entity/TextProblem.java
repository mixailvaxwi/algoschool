package com.algoschool.module_assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "step_text_problems")
@Getter
@Setter
public class TextProblem extends Step {

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    // Например, "EXACT" или "REGEX" (по умолчанию точное совпадение)
    @Column(name = "match_type")
    private String matchType = "EXACT";
}