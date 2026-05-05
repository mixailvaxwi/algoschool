package com.mpanyavin.algoschool.module_assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "problem_code")
@Getter
@Setter
@NoArgsConstructor
public class CodeProblem extends Problem {

    @Column(name = "allowed_languages", nullable = false)
    private String allowedLanguages;

    @Column(name = "time_limit_sec", nullable = false)
    private Integer timeLimitSec = 2;

    @Column(name = "memory_limit_mb", nullable = false)
    private Integer memoryLimitMb = 256;
}