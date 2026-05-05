package com.algoschool.module_course.entity;

import com.algoschool.module_user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Тип доступа (открытый или по заявкам)
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, columnDefinition = "varchar(255) default 'OPEN'")
    @Builder.Default
    private AccessType accessType = AccessType.OPEN;

    // СТАТУС ПУБЛИКАЦИИ (по умолчанию false - черновик)
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = false;

    // Связь с автором (преподавателем)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    // Каскадная связь с модулями (удалили курс -> удалились модули)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    @Builder.Default
    @JsonIgnore
    private List<Module> modules = new ArrayList<>();
}