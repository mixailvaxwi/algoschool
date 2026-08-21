package com.algoschool.problem.entity;

import com.algoschool.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Задача как единица содержания и оценивания — то, что живёт в банке задач.
 * <p>
 * Раньше задача наследовалась от {@code Step} и потому физически принадлежала
 * одному уроку: поставить её в другой урок можно было только копией, а копия
 * расходилась с оригиналом при правке условия и уводила часть счётчиков
 * попыток и решений в сторону. Теперь задача не знает ни про урок, ни про
 * курс — место в уроке описывает отдельная сущность
 * {@link com.algoschool.step.entity.ProblemStep}.
 */
@Entity
@Table(name = "problems")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Короткое имя для списка банка: по условию задачи в списке не сориентируешься. */
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    /** Владелец задачи: только он правит и удаляет её, независимо от курсов. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProblemVisibility visibility = ProblemVisibility.PRIVATE;

    /**
     * Вес задачи в баллах. Пока прогресс бинарный, поле только хранится:
     * начисление баллов — следующий этап, но добавлять колонку отдельной
     * миграцией к уже разделённой модели дороже, чем завести её сразу.
     */
    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 1;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "problem_tags", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "tag")
    private Set<String> tags = new LinkedHashSet<>();

    // Счётчики считают людей, а не попытки, и принадлежат задаче, а не её
    // размещению: одна задача в трёх уроках — одна общая статистика.
    @Column(name = "attempted_students_count", nullable = false)
    private Integer attemptedStudentsCount = 0;

    @Column(name = "success_students_count", nullable = false)
    private Integer successStudentsCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
