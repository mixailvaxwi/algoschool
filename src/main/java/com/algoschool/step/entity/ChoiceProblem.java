// ChoiceProblem.java
package com.algoschool.step.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "choice_problems")
@Getter @Setter
public class ChoiceProblem extends Problem { // НАСЛЕДУЕТСЯ ОТ PROBLEM!
    @ElementCollection
    @CollectionTable(name = "choice_problem_options", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "option_text")
    private List<String> options;

    // Множество индексов, а не один: при isMultipleChoice == true правильных
    // вариантов может быть несколько. Для одиночного выбора в множестве всегда
    // ровно один элемент.
    @ElementCollection
    @CollectionTable(name = "choice_problem_correct_options", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "option_index")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Integer> correctOptionIndexes;

    @Column(name = "is_multiple_choice")
    private boolean isMultipleChoice;
}