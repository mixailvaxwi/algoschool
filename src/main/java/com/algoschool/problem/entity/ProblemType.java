package com.algoschool.problem.entity;

import com.algoschool.exception.AppException;

/**
 * Тип задачи в терминах API. Имена совпадают с теми, что фронтенд использовал
 * для типов шага, — разделение задачи и размещения не должно менять словарь,
 * на котором говорит клиент.
 */
public enum ProblemType {
    CHOICE_PROBLEM,
    INPUT_PROBLEM,
    CODE_PROBLEM;

    public static ProblemType of(Problem problem) {
        if (problem instanceof ChoiceProblem) return CHOICE_PROBLEM;
        if (problem instanceof TextProblem) return INPUT_PROBLEM;
        if (problem instanceof CodeProblem) return CODE_PROBLEM;
        throw new IllegalStateException("Неизвестный тип задачи: " + problem.getClass().getSimpleName());
    }

    /** Пустая сущность нужного подтипа — под неё потом раскладываются поля запроса. */
    public Problem newInstance() {
        return switch (this) {
            case CHOICE_PROBLEM -> new ChoiceProblem();
            case INPUT_PROBLEM -> new TextProblem();
            case CODE_PROBLEM -> new CodeProblem();
        };
    }

    /** Подкласс для фильтра по типу в Criteria API (<code>type(p) = ?</code>). */
    public Class<? extends Problem> entityClass() {
        return switch (this) {
            case CHOICE_PROBLEM -> ChoiceProblem.class;
            case INPUT_PROBLEM -> TextProblem.class;
            case CODE_PROBLEM -> CodeProblem.class;
        };
    }

    public static ProblemType parse(String raw) {
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw AppException.badRequest("Неизвестный тип задачи: " + raw);
        }
    }
}
