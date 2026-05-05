package com.mpanyavin.algoschool.module_assessment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problem_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с базовой задачей
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // Текст варианта ответа
    @Column(nullable = false, length = 500)
    private String text;

    // Является ли этот вариант правильным
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}