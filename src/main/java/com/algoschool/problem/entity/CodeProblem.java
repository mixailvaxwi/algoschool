package com.algoschool.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "code_problems")
@Getter @Setter
public class CodeProblem extends Problem {
    @Column(name = "time_limit_sec")
    private Integer timeLimit;

    @Column(name = "memory_limit_mb")
    private Integer memoryLimit;

    @Column(name = "allowed_languages")
    private String allowedLanguages;

    @Column(name = "ejudge_contest_id")
    private Integer ejudgeContestId;

    @Column(name = "ejudge_problem_id")
    private String ejudgeProblemId;
}
