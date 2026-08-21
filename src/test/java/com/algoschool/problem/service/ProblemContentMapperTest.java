package com.algoschool.problem.service;

import com.algoschool.exception.AppException;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.entity.ChoiceProblem;
import com.algoschool.problem.entity.ProblemVisibility;
import com.algoschool.problem.entity.TextProblem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Правила содержания задачи. Проверять их важно именно здесь: задачу заводят
 * два разных экрана — банк и редактор урока, — и оба идут через этот класс.
 */
class ProblemContentMapperTest {

    private final ProblemContentMapper mapper = new ProblemContentMapper();

    private ProblemRequest choiceRequest(List<String> options, List<Integer> correct, boolean multiple) {
        ProblemRequest request = new ProblemRequest();
        request.setProblemType("CHOICE_PROBLEM");
        request.setTitle("Заголовок");
        request.setDescription("Условие");
        request.setOptions(options);
        request.setCorrectOptionIndexes(correct);
        request.setIsMultipleChoice(multiple);
        return request;
    }

    // --- Варианты ответа ---------------------------------------------------

    @Test
    void acceptsMultipleCorrectOptions() {
        ChoiceProblem problem = new ChoiceProblem();

        mapper.apply(problem, choiceRequest(List.of("a", "b", "c"), List.of(0, 2), true));

        assertThat(problem.getCorrectOptionIndexes()).containsExactlyInAnyOrder(0, 2);
        assertThat(problem.isMultipleChoice()).isTrue();
    }

    @Test
    void rejectsSecondCorrectOptionForSingleChoice() {
        assertThatThrownBy(() -> mapper.apply(new ChoiceProblem(), choiceRequest(List.of("a", "b"), List.of(0, 1), false)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("одиночного выбора");
    }

    @Test
    void rejectsEmptyCorrectOptions() {
        assertThatThrownBy(() -> mapper.apply(new ChoiceProblem(), choiceRequest(List.of("a", "b"), List.of(), true)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("хотя бы один правильный");
    }

    /**
     * Правильные ответы заданы индексами в присланном списке. Если бы пустой
     * вариант просто выбрасывался, отметка «правильный» съезжала бы на соседний.
     */
    @Test
    void rejectsBlankOptionInsteadOfDroppingIt() {
        assertThatThrownBy(() -> mapper.apply(new ChoiceProblem(), choiceRequest(List.of("a", "  ", "c"), List.of(2), false)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Пустой вариант");
    }

    @Test
    void rejectsCorrectIndexOutsideOptions() {
        assertThatThrownBy(() -> mapper.apply(new ChoiceProblem(), choiceRequest(List.of("a", "b"), List.of(5), false)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("за пределы");
    }

    @Test
    void rejectsSingleOption() {
        assertThatThrownBy(() -> mapper.apply(new ChoiceProblem(), choiceRequest(List.of("a"), List.of(0), false)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("два варианта");
    }

    // --- Общие поля ---------------------------------------------------------

    @Test
    void takesTitleFromFirstLineOfDescriptionWhenNotGiven() {
        // Так задачу заводит редактор урока: там форма шага, поля названия нет.
        assertThat(mapper.requireTitle(null, "Найдите путь\nв графе"))
                .isEqualTo("Найдите путь в графе");
    }

    @Test
    void keepsExplicitTitle() {
        assertThat(mapper.requireTitle("  Кратчайший путь ", "Условие")).isEqualTo("Кратчайший путь");
    }

    @Test
    void requiresTitleOrDescription() {
        assertThatThrownBy(() -> mapper.requireTitle("  ", "   "))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("название");
    }

    @Test
    void normalizesTagsToLowerCaseWithoutDuplicates() {
        ProblemRequest request = new ProblemRequest();
        request.setProblemType("INPUT_PROBLEM");
        request.setTitle("Задача");
        request.setDescription("Условие");
        request.setCorrectAnswer("42");
        request.setTags(List.of("Графы", " графы ", "DFS", ""));

        TextProblem problem = new TextProblem();
        mapper.apply(problem, request);

        assertThat(problem.getTags()).containsExactlyInAnyOrder("графы", "dfs");
    }

    @Test
    void defaultsToPrivateVisibilityAndSingleScore() {
        ProblemRequest request = new ProblemRequest();
        request.setProblemType("INPUT_PROBLEM");
        request.setTitle("Задача");
        request.setDescription("Условие");
        request.setCorrectAnswer("42");

        TextProblem problem = new TextProblem();
        mapper.apply(problem, request);

        assertThat(problem.getVisibility()).isEqualTo(ProblemVisibility.PRIVATE);
        assertThat(problem.getMaxScore()).isEqualTo(1);
    }

    @Test
    void requiresCorrectAnswerForTextProblem() {
        ProblemRequest request = new ProblemRequest();
        request.setProblemType("INPUT_PROBLEM");
        request.setTitle("Задача");
        request.setDescription("Условие");

        assertThatThrownBy(() -> mapper.apply(new TextProblem(), request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("правильный ответ");
    }
}
