package com.algoschool.problem.service;

import com.algoschool.exception.AppException;
import com.algoschool.problem.dto.ProblemDto;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
        }
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
