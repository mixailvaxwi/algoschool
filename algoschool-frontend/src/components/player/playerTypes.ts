export type StepType =
    | 'THEORY'
    | 'CODE_PROBLEM'
    | 'INPUT_PROBLEM'
    | 'CHOICE_PROBLEM'
    | 'NUMERIC_PROBLEM'
    | 'MATCHING_PROBLEM'
    | 'ORDERING_PROBLEM'
    | 'OPEN_ANSWER_PROBLEM';

export interface BaseStep {
    id: number;
    orderIndex: number;
    stepType: StepType;
}

export interface TheoryStep extends BaseStep {
    stepType: 'THEORY';
    content: string;
}

export interface BaseProblemStep extends BaseStep {
    description: string;
}

export interface CodeProblemStep extends BaseProblemStep {
    stepType: 'CODE_PROBLEM';
    timeLimitSec: number;
    memoryLimitMb: number;
    allowedLanguages: string;
}

export interface InputProblemStep extends BaseProblemStep {
    stepType: 'INPUT_PROBLEM';
}

export interface ChoiceProblemStep extends BaseProblemStep {
    stepType: 'CHOICE_PROBLEM';
    isMultipleChoice: boolean;
    options: string[];
}

export interface NumericProblemStep extends BaseProblemStep {
    stepType: 'NUMERIC_PROBLEM';
    /** Допуск показывается студенту: иначе непонятно, до скольких знаков округлять. */
    tolerance: number;
    toleranceKind: 'ABSOLUTE' | 'RELATIVE';
}

export interface MatchingProblemStep extends BaseProblemStep {
    stepType: 'MATCHING_PROBLEM';
    leftItems: string[];
    /** Уже перемешаны сервером — в порядке хранения это был бы готовый ответ. */
    rightItems: string[];
}

export interface OrderingProblemStep extends BaseProblemStep {
    stepType: 'ORDERING_PROBLEM';
    /** Тоже перемешаны сервером. */
    items: string[];
}

export interface OpenAnswerProblemStep extends BaseProblemStep {
    stepType: 'OPEN_ANSWER_PROBLEM';
}

// Одиночный выбор — payload "2"; множественный — "0,2" (без пробелов, порядок неважен).
export const buildChoicePayload = (selected: Set<number>): string =>
    [...selected].sort((a, b) => a - b).join(',');

/** Упорядочивание: индексы показанных элементов в том порядке, в каком их расставил студент. */
export const buildOrderingPayload = (order: number[]): string => order.join(',');

/**
 * Соответствие: пары «левый индекс-показанный правый индекс».
 * Незаполненные строки пропускаются — сервер посчитает их несопоставленными.
 */
export const buildMatchingPayload = (choices: Record<number, number | undefined>): string =>
    Object.entries(choices)
        .filter(([, right]) => right !== undefined)
        .map(([left, right]) => `${left}-${right}`)
        .join(',');

export type AnyStep =
    | TheoryStep
    | CodeProblemStep
    | InputProblemStep
    | ChoiceProblemStep
    | NumericProblemStep
    | MatchingProblemStep
    | OrderingProblemStep
    | OpenAnswerProblemStep;

export interface FullLesson {
    id: number;
    title: string;
    orderIndex: number;
    steps: AnyStep[];
}
