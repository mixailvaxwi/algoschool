// ChoiceProblem.java
package com.algoschool.step.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "choice_problems")
@Getter @Setter
public class ChoiceProblem extends Problem { // НАСЛЕДУЕТСЯ ОТ PROBLEM!
    @ElementCollection
    @CollectionTable(name = "choice_problem_options", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "option_text")
    private List<String> options;

    @Column(name = "correct_option_index", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer correctOptionIndex;

    @Column(name = "is_multiple_choice")
    private boolean isMultipleChoice;
}