package com.algoschool.step.entity;

import com.algoschool.course.entity.Lesson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Шаг урока: место в программе курса.
 * <p>
 * Подтипов два — теория держит текст прямо в себе, а
 * {@link ProblemStep} лишь ссылается на задачу из банка. Наружу сущность
 * не отдаётся: и плеер, и редактор преподавателя работают через DTO.
 */
@Entity
@Table(name = "steps")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public abstract class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonIgnore
    private Lesson lesson;
}
