package com.algoschool.grade.entity;

import com.algoschool.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Зачтённый балл студента за элемент журнала.
 * <p>
 * Отдельная сущность, а не поле у решения: попыток много, зачёт один, и какая
 * из попыток идёт в зачёт, решает {@link GradePolicy} элемента.
 */
@Entity
@Table(name = "grades")
@Getter
@Setter
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_item_id", nullable = false)
    private GradeItem gradeItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer score;

    /**
     * Оценку поставил человек. Пересчёт по решениям такую оценку не трогает:
     * иначе следующая отправка студента молча стёрла бы решение преподавателя.
     */
    @Column(name = "is_manual", nullable = false)
    private boolean isManual;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
