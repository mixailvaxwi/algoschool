package com.algoschool.step.entity;

import com.algoschool.problem.entity.Problem;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Размещение задачи из банка в уроке.
 * <p>
 * Шаг отвечает за место — урок и позицию в нём; содержание и правильный ответ
 * лежат в {@link Problem} и общие для всех размещений. Удаление шага снимает
 * задачу с урока и не трогает саму задачу.
 */
@Entity
@Table(name = "problem_steps")
@Getter
@Setter
public class ProblemStep extends Step {

    /**
     * Загружается сразу, а не лениво, и это принципиально.
     * <p>
     * {@link Problem} — абстрактный корень JOINED-иерархии, и ленивая ссылка
     * дала бы прокси базового типа: {@code problem instanceof ChoiceProblem}
     * возвращал бы false, а по подтипу задачи выбирается и проверяющий модуль,
     * и плеерное DTO. Пока задача была шагом, такого не было — шаг грузился
     * сразу конкретным подтипом.
     * <p>
     * На стоимость это не влияет: шаг с задачей без самой задачи бесполезен —
     * её условие нужно и плееру, и редактору.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
}
