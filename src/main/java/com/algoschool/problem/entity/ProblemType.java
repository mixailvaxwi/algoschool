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
    CODE_PROBLEM,
    NUMERIC_PROBLEM,
    MATCHING_PROBLEM,
    ORDERING_PROBLEM,
    OPEN_ANSWER_PROBLEM;

    public static ProblemType of(Problem problem) {
        if (problem instanceof ChoiceProblem) return CHOICE_PROBLEM;
        if (problem instanceof TextProblem) return INPUT_PROBLEM;
        if (problem instanceof CodeProblem) return CODE_PROBLEM;
        if (problem instanceof NumericProblem) return NUMERIC_PROBLEM;
        if (problem instanceof MatchingProblem) return MATCHING_PROBLEM;
        if (problem instanceof OrderingProblem) return ORDERING_PROBLEM;
        if (problem instanceof OpenAnswerProblem) return OPEN_ANSWER_PROBLEM;
        throw new IllegalStateException("Неизвестный тип задачи: " + problem.getClass().getSimpleName());
    }

    /** Пустая сущность нужного подтипа — под неё потом раскладываются поля запроса. */
    public Problem newInstance() {
        return switch (this) {
            case CHOICE_PROBLEM -> new ChoiceProblem();
            case INPUT_PROBLEM -> new TextProblem();
            case CODE_PROBLEM -> new CodeProblem();
            case NUMERIC_PROBLEM -> new NumericProblem();
            case MATCHING_PROBLEM -> new MatchingProblem();
            case ORDERING_PROBLEM -> new OrderingProblem();
            case OPEN_ANSWER_PROBLEM -> new OpenAnswerProblem();
        };
    }

    /** Подкласс для фильтра по типу в Criteria API (<code>type(p) = ?</code>). */
    public Class<? extends Problem> entityClass() {
        return switch (this) {
            case CHOICE_PROBLEM -> ChoiceProblem.class;
            case INPUT_PROBLEM -> TextProblem.class;
            case CODE_PROBLEM -> CodeProblem.class;
            case NUMERIC_PROBLEM -> NumericProblem.class;
            case MATCHING_PROBLEM -> MatchingProblem.class;
            case ORDERING_PROBLEM -> OrderingProblem.class;
            case OPEN_ANSWER_PROBLEM -> OpenAnswerProblem.class;
        };
    }

    /**
     * Проверяет ли задачу человек. Такие решения ждут не Ejudge, а
     * преподавателя, и попадают в очередь проверки.
     */
    public boolean isManuallyReviewed() {
        return this == OPEN_ANSWER_PROBLEM;
    }

    public static ProblemType parse(String raw) {
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw AppException.badRequest("Неизвестный тип задачи: " + raw);
        }
    }
}
