package com.algoschool.grade.entity;

import com.algoschool.course.entity.Course;
import com.algoschool.step.entity.ProblemStep;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Оцениваемый элемент курса — столбец журнала оценок.
 * <p>
 * Для задачи элемент заводится вместе с её размещением в уроке и снимается
 * вместе с ним. {@link #maxScore} — это вес элемента <i>в этом курсе</i>, а не
 * свойство задачи: одна и та же задача из банка может стоить в разных курсах
 * по-разному, и балл попытки пересчитывается в шкалу элемента.
 */
@Entity
@Table(name = "grade_items")
@Getter
@Setter
public class GradeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GradeItemKind kind;

    /** Заполнено только у {@link GradeItemKind#PROBLEM}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private ProblemStep step;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GradePolicy policy = GradePolicy.BEST;

    /** Порядок столбцов в журнале: у задач повторяет порядок прохождения курса. */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
