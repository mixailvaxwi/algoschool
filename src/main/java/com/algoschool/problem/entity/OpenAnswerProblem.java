package com.algoschool.problem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Развёрнутый ответ: проверяет человек, а не платформа.
 * <p>
 * Решение уходит в статус PENDING_REVIEW и ждёт преподавателя — в отличие от
 * PENDING, за которым стоит Ejudge и который разбирает фоновый опрос.
 */
@Entity
@Table(name = "open_answer_problems")
@Getter @Setter
public class OpenAnswerProblem extends Problem {

    /** Критерии для проверяющего. Студенту не отдаются никогда. */
    @Column(name = "review_guidelines", columnDefinition = "TEXT")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String reviewGuidelines;
}
