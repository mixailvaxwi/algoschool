package com.algoschool.problem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Сопоставить элементы левой колонки элементам правой.
 * <p>
 * Пары хранятся в правильном виде: {@code leftItems.get(i)} соответствует
 * {@code rightItems.get(i)} — автор так их и вводит, парами. Студенту правая
 * колонка показывается перемешанной по {@link #rightDisplayOrder}, иначе
 * ответ читался бы прямо из выдачи API.
 */
@Entity
@Table(name = "matching_problems")
@Getter @Setter
public class MatchingProblem extends Problem {

    @ElementCollection
    @CollectionTable(name = "matching_problem_left", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "position")
    @Column(name = "text")
    private List<String> leftItems = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "matching_problem_right", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "position")
    @Column(name = "text")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> rightItems = new ArrayList<>();

    /**
     * {@code rightDisplayOrder.get(k)} — индекс правого элемента, который
     * студент видит на k-м месте.
     */
    @ElementCollection
    @CollectionTable(name = "matching_problem_display_order", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "position")
    @Column(name = "right_index")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> rightDisplayOrder = new ArrayList<>();

    /** Правая колонка в том виде, в каком её видит студент. */
    public List<String> rightItemsForDisplay() {
        List<String> shown = new ArrayList<>(rightDisplayOrder.size());
        for (Integer index : rightDisplayOrder) {
            shown.add(rightItems.get(index));
        }
        return shown;
    }
}
