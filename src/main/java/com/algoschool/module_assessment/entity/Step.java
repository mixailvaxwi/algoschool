package com.mpanyavin.algoschool.module_assessment.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mpanyavin.algoschool.module_course.entity.Lesson;
import jakarta.persistence.*;import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "steps")
@Inheritance(strategy = InheritanceType.JOINED)
// Настройки Jackson для корректной генерации JSON
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "stepType" // Фронтенд будет читать это поле, чтобы понять, как рисовать UI
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Theory.class, name = "THEORY"),
        @JsonSubTypes.Type(value = InputProblem.class, name = "INPUT_PROBLEM"),
        @JsonSubTypes.Type(value = ChoiceProblem.class, name = "CHOICE_PROBLEM"),
        @JsonSubTypes.Type(value = CodeProblem.class, name = "CODE_PROBLEM")
})@Getter
@Setter
@NoArgsConstructor
public abstract class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}