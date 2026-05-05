export type StepType = 'THEORY' | 'INPUT_PROBLEM' | 'CHOICE_PROBLEM' | 'CODE_PROBLEM';

// Базовый интерфейс, который есть у всех шагов
export interface BaseStep {
    id: number;
    orderIndex: number;
    stepType: StepType;
}

export interface TheoryStep extends BaseStep {
    stepType: 'THEORY';
    content: string; // Markdown или HTML текст
}

// Общие поля для всех задач (наследников Problem)
export interface BaseProblemStep extends BaseStep {
    description: string;
    difficulty: number;
    xpReward: number;
    attemptedStudentsCount: number;
    successStudentsCount: number;
}

export interface InputProblemStep extends BaseProblemStep {
    stepType: 'INPUT_PROBLEM';
    isCaseSensitive: boolean;
}

export interface ChoiceProblemStep extends BaseProblemStep {
    stepType: 'CHOICE_PROBLEM';
    isMultipleChoice: boolean;
    // Примечание: на бэкенде нам нужно будет отдавать список вариантов (без флага isCorrect!)
    options?: { id: number; text: string }[];
}

export interface CodeProblemStep extends BaseProblemStep {
    stepType: 'CODE_PROBLEM';
    allowedLanguages: string;
    timeLimitSec: number;
    memoryLimitMb: number;
}

// Объединяем всё в один тип (Discriminated Union)
export type Step = TheoryStep | InputProblemStep | ChoiceProblemStep | CodeProblemStep;