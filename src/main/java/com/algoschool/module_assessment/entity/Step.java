package com.algoschool.module_assessment.entity;

import com.algoschool.module_course.entity.Lesson;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "steps")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Theory.class, name = "theory"),
        @JsonSubTypes.Type(value = CodeProblem.class, name = "code"),
        @JsonSubTypes.Type(value = TextProblem.class, name = "text"),
        @JsonSubTypes.Type(value = ChoiceProblem.class, name = "choice"),
        @JsonSubTypes.Type(value = FileProblem.class, name = "file")
})
public abstract class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_index", nullable = false)
    private Integer positionIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}