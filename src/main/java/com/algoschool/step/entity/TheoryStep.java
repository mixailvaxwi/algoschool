// TheoryStep.java
package com.algoschool.step.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "theory_steps")
@Getter @Setter
public class TheoryStep extends Step {
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}