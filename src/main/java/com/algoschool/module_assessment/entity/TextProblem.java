package com.algoschool.module_assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "problem_input")
@Getter
@Setter
public class InputProblem extends Problem {

    // Ожидаемый точный ответ от студента
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

}