package com.algoschool.problem.service;

import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.entity.ProblemType;
import com.algoschool.step.dto.StepCreateRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Форма шага урока и карточка банка описывают одно и то же содержание задачи
 * двумя разными DTO, и одно перекладывается в другое поле за полем.
 * <p>
 * Забытое поле такую копию не ломает — оно просто молча приезжает пустым.
 * Именно так после добавления новых типов задач числовой ответ стал уходить
 * в банк без правильного значения, а развёрнутый — без критериев проверки.
 * Тест перебирает поля рефлексией, поэтому новый тип задачи, добавленный без
 * копирования, роняет сборку вместо того, чтобы тихо потерять данные.
 */
class StepRequestCopyTest {

    /** Заполняется отдельно: у шага это stepType, у задачи — problemType. */
    private static final String TYPE_FIELD = "problemType";

    private final ProblemContentMapper mapper = new ProblemContentMapper();

    @Test
    void everyProblemFieldOfStepRequestReachesTheBank() throws Exception {
        StepCreateRequest step = new StepCreateRequest();
        step.setStepType(ProblemType.INPUT_PROBLEM.name());
        step.setOrderIndex(1);

        List<Field> copiedFields = new ArrayList<>();
        for (Field target : ProblemRequest.class.getDeclaredFields()) {
            if (target.isSynthetic() || TYPE_FIELD.equals(target.getName())) {
                continue;
            }
            Field source = StepCreateRequest.class.getDeclaredField(target.getName());
            assertThat(source.getType())
                    .as("тип поля %s должен совпадать в обоих DTO", target.getName())
                    .isEqualTo(target.getType());

            source.setAccessible(true);
            source.set(step, sampleValue(source.getType()));
            copiedFields.add(target);
        }

        ProblemRequest copy = mapper.fromStepRequest(step, ProblemType.INPUT_PROBLEM);

        assertThat(copy.getProblemType()).isEqualTo(ProblemType.INPUT_PROBLEM.name());
        for (Field target : copiedFields) {
            target.setAccessible(true);
            Field source = StepCreateRequest.class.getDeclaredField(target.getName());
            source.setAccessible(true);

            assertThat(target.get(copy))
                    .as("поле %s не перенесено из формы шага в задачу банка", target.getName())
                    .isEqualTo(source.get(step));
        }

        // Страховка от вырождения: если поля вдруг перестанут находиться,
        // цикл выше отработает вхолостую и тест ничего не проверит.
        assertThat(copiedFields).hasSizeGreaterThan(15);
    }

    /** Различимое значение нужного типа — лишь бы отличалось от null и от нуля. */
    private Object sampleValue(Class<?> type) {
        if (type == String.class) return "значение-" + type.hashCode();
        if (type == Integer.class) return 7;
        if (type == Double.class) return 1.5;
        if (type == Boolean.class) return Boolean.TRUE;
        if (type == List.class) return List.of("элемент");
        throw new IllegalStateException(
                "В ProblemRequest появился тип поля, который тест не умеет заполнять: " + type);
    }
}
