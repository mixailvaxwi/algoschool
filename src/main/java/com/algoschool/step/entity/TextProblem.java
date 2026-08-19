// TextProblem.java
package com.algoschool.step.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "text_problems")
@Getter @Setter
public class TextProblem extends Problem { // НАСЛЕДУЕТСЯ ОТ PROBLEM!
    @Column(name = "correct_answer", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String correctAnswer;
}