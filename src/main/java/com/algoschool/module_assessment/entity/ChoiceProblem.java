package com.mpanyavin.algoschool.module_assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "problem_choice")
@Getter
@Setter
@NoArgsConstructor
public class ChoiceProblem extends Problem {

    @Column(name = "is_multiple_choice", nullable = false)
    private Boolean isMultipleChoice = false;
}