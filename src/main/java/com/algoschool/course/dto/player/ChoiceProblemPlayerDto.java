package com.algoschool.course.dto.player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceProblemPlayerDto extends StepPlayerDto {
    private String description;
    private List<String> options;

    // Boolean, а не boolean: для примитивного поля Lombok генерирует геттер
    // isMultipleChoice(), а Jackson трактует любой is-геттер как boolean-свойство
    // и обрезает префикс "is" — в JSON поле уходило как "multipleChoice",
    // фронтенд читает "isMultipleChoice" и получал undefined. Со «Boolean»
    // геттер — getIsMultipleChoice(), и Jackson берёт имя свойства из него
    // без обрезки префикса. Тот же приём уже используется в StepTeacherDto
    // и StepCreateRequest.
    private Boolean isMultipleChoice;
    // ВАЖНО: Никакого correctOptionIndex!
}