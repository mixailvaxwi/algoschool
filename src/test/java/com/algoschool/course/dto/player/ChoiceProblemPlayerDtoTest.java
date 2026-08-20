package com.algoschool.course.dto.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Регрессия на разрыв между тем, что сохранил преподаватель, и тем, что видел
 * студент: для примитивного {@code boolean isMultipleChoice} Lombok генерирует
 * геттер {@code isMultipleChoice()}, а Jackson трактует любой is-геттер как
 * boolean-свойство и обрезает префикс "is" — поле уходило в JSON как
 * "multipleChoice". Фронтенд читает "isMultipleChoice" и получал undefined,
 * поэтому студент видел радиокнопки для теста, отмеченного преподавателем как
 * множественный выбор.
 * <p>
 * Простое добавление {@code @JsonProperty("isMultipleChoice")} на примитивное
 * поле не решает проблему до конца: Jackson не связывает поле и авто-геттер
 * с разными именами в одно свойство и сериализует оба — получаются два ключа
 * ("multipleChoice" и "isMultipleChoice") одновременно. Поэтому тип поля
 * заменён на {@code Boolean} (как в StepTeacherDto/StepCreateRequest) —
 * тогда геттер называется getIsMultipleChoice(), и лишнего ключа не возникает.
 */
class ChoiceProblemPlayerDtoTest {

    @Test
    void serializesMultipleChoiceUnderExactlyOneKey() throws Exception {
        ChoiceProblemPlayerDto dto = new ChoiceProblemPlayerDto();
        dto.setIsMultipleChoice(true);

        String json = new ObjectMapper().writeValueAsString(dto);

        assertThat(json).contains("\"isMultipleChoice\":true");
        assertThat(json).doesNotContain("\"multipleChoice\"");
    }
}
