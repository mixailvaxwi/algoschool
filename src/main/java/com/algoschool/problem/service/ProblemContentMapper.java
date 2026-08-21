package com.algoschool.problem.service;

import com.algoschool.exception.AppException;
import com.algoschool.problem.dto.ProblemDto;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.entity.*;
import com.algoschool.step.dto.StepCreateRequest;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Единственное место, где содержание задачи перекладывается из запроса в
 * сущность и обратно.
 * <p>
 * Задачу можно завести двумя путями — прямо в банке и попутно, из редактора
 * урока, — и оба должны проверять условие одинаково. Раньше такая проверка
 * жила только в StepServiceImpl, и второй путь неизбежно разъехался бы с ним.
 */
@Component
public class ProblemContentMapper {

    private static final int MAX_TITLE_LENGTH = 200;

    private static final Random RANDOM = new SecureRandom();

    /**
     * Собирает запрос к банку из формы шага урока.
     * <p>
     * Механическое копирование поле в поле, и потому оно здесь, а не в
     * StepServiceImpl: полей содержания уже больше двадцати, и забытое поле не
     * ломает сборку — оно молча приезжает пустым. Так уже случилось однажды с
     * числовым ответом, поэтому список полей закреплён тестом.
     */
    public ProblemRequest fromStepRequest(StepCreateRequest step, ProblemType type) {
        ProblemRequest request = new ProblemRequest();
        request.setProblemType(type.name());
        request.setTitle(requireTitle(step.getTitle(), step.getDescription()));
        request.setDescription(step.getDescription());
        request.setDifficulty(step.getDifficulty());
        request.setVisibility(step.getVisibility());
        request.setMaxScore(step.getMaxScore());
        request.setTags(step.getTags());

        request.setOptions(step.getOptions());
        request.setCorrectOptionIndexes(step.getCorrectOptionIndexes());
        request.setIsMultipleChoice(step.getIsMultipleChoice());

        request.setCorrectAnswer(step.getCorrectAnswer());

        request.setCorrectValue(step.getCorrectValue());
        request.setTolerance(step.getTolerance());
        request.setToleranceKind(step.getToleranceKind());

        request.setLeftItems(step.getLeftItems());
        request.setRightItems(step.getRightItems());
        request.setOrderedItems(step.getOrderedItems());
        request.setReviewGuidelines(step.getReviewGuidelines());

        request.setTimeLimitSec(step.getTimeLimitSec());
        request.setMemoryLimitMb(step.getMemoryLimitMb());
        request.setAllowedLanguages(step.getAllowedLanguages());
        request.setEjudgeContestId(step.getEjudgeContestId());
        request.setEjudgeProblemId(step.getEjudgeProblemId());

        return request;
    }

    // --- Запрос -> сущность -----------------------------------------------

    /**
     * Раскладывает поля запроса по задаче. Тип задачи здесь не меняется:
     * подтип определяет таблицу, поэтому сменить его можно только пересозданием.
     */
    public void apply(Problem problem, ProblemRequest request) {
        problem.setTitle(requireTitle(request.getTitle(), request.getDescription()));
        problem.setDescription(request.getDescription());
        problem.setDifficulty(parseDifficulty(request.getDifficulty()));
        problem.setVisibility(parseVisibility(request.getVisibility()));
        problem.setMaxScore(request.getMaxScore() == null ? 1 : request.getMaxScore());
        problem.setTags(normalizeTags(request.getTags()));

        if (problem instanceof ChoiceProblem choice) {
            applyChoice(choice, request);
        } else if (problem instanceof TextProblem text) {
            applyText(text, request);
        } else if (problem instanceof CodeProblem code) {
            applyCode(code, request);
        } else if (problem instanceof NumericProblem numeric) {
            applyNumeric(numeric, request);
        } else if (problem instanceof MatchingProblem matching) {
            applyMatching(matching, request);
        } else if (problem instanceof OrderingProblem ordering) {
            applyOrdering(ordering, request);
        } else if (problem instanceof OpenAnswerProblem open) {
            open.setReviewGuidelines(request.getReviewGuidelines());
        }
    }

    private void applyNumeric(NumericProblem numeric, ProblemRequest request) {
        if (request.getCorrectValue() == null || !Double.isFinite(request.getCorrectValue())) {
            throw AppException.badRequest("Укажите правильное числовое значение");
        }
        double tolerance = request.getTolerance() == null ? 0.0 : request.getTolerance();
        if (tolerance < 0 || !Double.isFinite(tolerance)) {
            throw AppException.badRequest("Допуск не может быть отрицательным");
        }

        numeric.setCorrectValue(request.getCorrectValue());
        numeric.setTolerance(tolerance);
        numeric.setToleranceKind(ToleranceKind.parse(request.getToleranceKind()));
    }

    private void applyMatching(MatchingProblem matching, ProblemRequest request) {
        List<String> left = request.getLeftItems() == null ? List.of() : request.getLeftItems();
        List<String> right = request.getRightItems() == null ? List.of() : request.getRightItems();

        if (left.size() != right.size()) {
            throw AppException.badRequest("Левая и правая колонки должны быть одной длины");
        }
        if (left.size() < 2) {
            throw AppException.badRequest("Нужно как минимум две пары");
        }
        requireNoBlanks(left, "Пустой элемент левой колонки: заполните или удалите строку");
        requireNoBlanks(right, "Пустой элемент правой колонки: заполните или удалите строку");

        matching.setLeftItems(new ArrayList<>(left));
        matching.setRightItems(new ArrayList<>(right));
        matching.setRightDisplayOrder(shuffledOrder(right.size()));
    }

    private void applyOrdering(OrderingProblem ordering, ProblemRequest request) {
        List<String> items = request.getOrderedItems() == null ? List.of() : request.getOrderedItems();
        if (items.size() < 2) {
            throw AppException.badRequest("Нужно как минимум два элемента для упорядочивания");
        }
        requireNoBlanks(items, "Пустой элемент: заполните или удалите строку");

        ordering.setItems(new ArrayList<>(items));
        ordering.setDisplayOrder(shuffledOrder(items.size()));
    }

    private void requireNoBlanks(List<String> values, String message) {
        if (values.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw AppException.badRequest(message);
        }
    }

    /**
     * Перестановка для показа студенту.
     * <p>
     * Автор вводит содержание в правильном виде — так удобнее и ему, и правке.
     * Наружу оно отдаётся перемешанным, иначе ответ читался бы прямо из выдачи
     * API. Тождественная перестановка отбрасывается: она не перемешивает
     * ничего, а вероятность её выпадения на трёх элементах — каждый шестой раз.
     */
    private List<Integer> shuffledOrder(int size) {
        List<Integer> order = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            order.add(i);
        }
        if (size < 2) {
            return order;
        }

        java.util.Collections.shuffle(order, RANDOM);
        boolean identity = true;
        for (int i = 0; i < size && identity; i++) {
            identity = order.get(i) == i;
        }
        if (identity) {
            java.util.Collections.rotate(order, 1);
        }
        return order;
    }

    private void applyChoice(ChoiceProblem choice, ProblemRequest request) {
        List<String> options = request.getOptions() == null ? List.of() : request.getOptions();
        if (options.size() < 2) {
            throw AppException.badRequest("Нужно как минимум два варианта ответа");
        }
        if (options.stream().anyMatch(option -> option == null || option.isBlank())) {
            // Выбрасывать пустые варианты молча нельзя: правильные ответы заданы
            // индексами, и после выбрасывания они указывали бы на чужие строки.
            throw AppException.badRequest("Пустой вариант ответа: заполните или удалите его");
        }

        List<Integer> correct = request.getCorrectOptionIndexes();
        if (correct == null || correct.isEmpty()) {
            throw AppException.badRequest("Нужно отметить хотя бы один правильный вариант");
        }
        if (correct.stream().anyMatch(index -> index == null || index < 0 || index >= options.size())) {
            throw AppException.badRequest("Правильный вариант указывает за пределы списка вариантов");
        }

        boolean multiple = Boolean.TRUE.equals(request.getIsMultipleChoice());
        Set<Integer> correctSet = new LinkedHashSet<>(correct);
        if (!multiple && correctSet.size() > 1) {
            throw AppException.badRequest("Для одиночного выбора можно отметить только один правильный вариант");
        }

        choice.setOptions(new ArrayList<>(options));
        choice.setCorrectOptionIndexes(correctSet);
        choice.setMultipleChoice(multiple);
    }

    private void applyText(TextProblem text, ProblemRequest request) {
        if (request.getCorrectAnswer() == null || request.getCorrectAnswer().isBlank()) {
            throw AppException.badRequest("Укажите правильный ответ");
        }
        text.setCorrectAnswer(request.getCorrectAnswer());
    }

    private void applyCode(CodeProblem code, ProblemRequest request) {
        code.setTimeLimit(request.getTimeLimitSec());
        code.setMemoryLimit(request.getMemoryLimitMb());
        code.setAllowedLanguages(request.getAllowedLanguages());
        code.setEjudgeContestId(request.getEjudgeContestId());
        code.setEjudgeProblemId(request.getEjudgeProblemId());
    }

    // --- Сущность -> DTO ---------------------------------------------------

    /**
     * @param withAnswers правильные ответы кладутся в DTO только для автора и
     *                    только в карточке задачи — в списках их нет никогда
     */
    public ProblemDto toDto(Problem problem, long usageCount, boolean editable, boolean withAnswers) {
        ProblemDto.ProblemDtoBuilder dto = ProblemDto.builder()
                .id(problem.getId())
                .problemType(ProblemType.of(problem).name())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty() == null ? null : problem.getDifficulty().name())
                .visibility(problem.getVisibility().name())
                .maxScore(problem.getMaxScore())
                .tags(List.copyOf(problem.getTags()))
                .authorId(problem.getAuthor().getId())
                .authorName(displayName(problem))
                .editable(editable)
                .usageCount(usageCount)
                .attemptedStudentsCount(problem.getAttemptedStudentsCount())
                .successStudentsCount(problem.getSuccessStudentsCount())
                .updatedAt(problem.getUpdatedAt());

        if (problem instanceof ChoiceProblem choice) {
            dto.options(List.copyOf(choice.getOptions()))
                    .isMultipleChoice(choice.isMultipleChoice());
            if (withAnswers) {
                dto.correctOptionIndexes(choice.getCorrectOptionIndexes().stream().sorted().toList());
            }
        } else if (problem instanceof TextProblem text) {
            if (withAnswers) {
                dto.correctAnswer(text.getCorrectAnswer());
            }
        } else if (problem instanceof CodeProblem code) {
            dto.timeLimitSec(code.getTimeLimit())
                    .memoryLimitMb(code.getMemoryLimit())
                    .allowedLanguages(code.getAllowedLanguages())
                    .ejudgeContestId(code.getEjudgeContestId())
                    .ejudgeProblemId(code.getEjudgeProblemId());
        } else if (problem instanceof NumericProblem numeric) {
            dto.tolerance(numeric.getTolerance())
                    .toleranceKind(numeric.getToleranceKind().name());
            if (withAnswers) {
                dto.correctValue(numeric.getCorrectValue());
            }
        } else if (problem instanceof MatchingProblem matching) {
            dto.leftItems(List.copyOf(matching.getLeftItems()));
            if (withAnswers) {
                // Правая колонка в порядке хранения — это и есть ответ:
                // i-й правый элемент подходит к i-му левому.
                dto.rightItems(List.copyOf(matching.getRightItems()));
            }
        } else if (problem instanceof OrderingProblem ordering) {
            if (withAnswers) {
                // Элементы хранятся в правильном порядке, поэтому сам список
                // и есть ответ.
                dto.orderedItems(List.copyOf(ordering.getItems()));
            }
        } else if (problem instanceof OpenAnswerProblem open) {
            if (withAnswers) {
                dto.reviewGuidelines(open.getReviewGuidelines());
            }
        }

        return dto.build();
    }

    // --- Разбор отдельных полей -------------------------------------------

    /**
     * Название обязательно, но из редактора урока его никто не вводит — там
     * форма шага, а не карточка задачи. В этом случае берём первую строку
     * условия: по тому же правилу названия получили задачи, созданные до
     * появления банка (см. V7).
     */
    public String requireTitle(String title, String description) {
        if (title != null && !title.isBlank()) {
            return trimTo(title.trim(), MAX_TITLE_LENGTH);
        }
        String fromDescription = description == null
                ? ""
                : description.replace('\r', ' ').replace('\n', ' ').trim();
        if (fromDescription.isBlank()) {
            throw AppException.badRequest("Укажите название задачи");
        }
        return trimTo(fromDescription, MAX_TITLE_LENGTH);
    }

    private Difficulty parseDifficulty(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Difficulty.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Неизвестная сложность: " + raw);
        }
    }

    private ProblemVisibility parseVisibility(String raw) {
        if (raw == null || raw.isBlank()) {
            return ProblemVisibility.PRIVATE;
        }
        try {
            return ProblemVisibility.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Неизвестная видимость: " + raw);
        }
    }

    private Set<String> normalizeTags(List<String> raw) {
        Set<String> tags = new LinkedHashSet<>();
        if (raw == null) {
            return tags;
        }
        for (String tag : raw) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            // Регистр и пробелы по краям иначе плодят «Графы», «графы» и «графы »
            // как три разных тега, и фильтр по тегу перестаёт что-либо значить.
            tags.add(trimTo(tag.trim().toLowerCase(), 50));
        }
        return tags;
    }

    private String trimTo(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String displayName(Problem problem) {
        String name = problem.getAuthor().getName();
        return name == null || name.isBlank() ? problem.getAuthor().getUsername() : name;
    }
}
