package com.algoschool.module_assessment.entity;

import com.algoschool.module_assessment.entity.Problem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problem_choice")
@Getter
@Setter
public class ChoiceProblem extends Problem {

    // Hibernate сам создаст отдельную таблицу для хранения этих строк!
    @ElementCollection(fetch = FetchType.EAGER) // EAGER нужен, чтобы варианты загружались сразу вместе с задачей
    @CollectionTable(
            name = "problem_choice_options", // Имя вспомогательной таблицы в БД
            joinColumns = @JoinColumn(name = "problem_id") // Как она будет ссылаться на эту задачу
    )
    @Column(name = "option_text", nullable = false) // Имя колонки с самим текстом варианта
    private List<String> options = new ArrayList<>();

    // Индекс правильного ответа (0 - первый вариант, 1 - второй и т.д.)
    @Column(name = "correct_option_index", nullable = false)
    private Integer correctOptionIndex;

}