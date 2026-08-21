package com.algoschool.problem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Расставить элементы в правильном порядке.
 * <p>
 * {@link #items} лежат в правильном порядке — так их вводит автор. Студенту
 * они показываются перемешанными по {@link #displayOrder}: без этой
 * перестановки задача решалась бы чтением ответа API сверху вниз.
 */
@Entity
@Table(name = "ordering_problems")
@Getter @Setter
public class OrderingProblem extends Problem {

    @ElementCollection
    @CollectionTable(name = "ordering_problem_items", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "position")
    @Column(name = "text")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> items = new ArrayList<>();

    /**
     * Порядок показа: {@code displayOrder.get(k)} — индекс элемента из
     * {@link #items}, который студент видит на k-м месте. Наружу не отдаётся —
     * иначе перестановка перестала бы что-либо скрывать.
     */
    @ElementCollection
    @CollectionTable(name = "ordering_problem_display_order", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "position")
    @Column(name = "item_index")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> displayOrder = new ArrayList<>();

    /** Элементы в том виде, в каком их видит студент. */
    public List<String> itemsForDisplay() {
        List<String> shown = new ArrayList<>(displayOrder.size());
        for (Integer index : displayOrder) {
            shown.add(items.get(index));
        }
        return shown;
    }
}
