package com.mpanyavin.algoschool.module_assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "step_theory")
@Getter
@Setter
@NoArgsConstructor
public class Theory extends Step {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}