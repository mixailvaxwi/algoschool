// Базовый интерфейс для всех шагов
export interface BaseStep {
    id: number;
    orderIndex: number;
    stepType: 'THEORY' | 'CODE_PROBLEM' | 'INPUT_PROBLEM' | 'CHOICE_PROBLEM';
}

// ТЕОРИЯ
export interface TheoryStep extends BaseStep {
    stepType: 'THEORY';
    content: string;
}

// Базовый интерфейс для ЗАДАЧ
export interface BaseProblemStep extends BaseStep {
    description: string;
    difficulty: number;
    xpReward: number;
}

// КОД
export interface CodeProblemStep extends BaseProblemStep {
    stepType: 'CODE_PROBLEM';
    timeLimitSec: number;
    memoryLimitMb: number;
    allowedLanguages: string;
}

// ВВОД СТРОКИ (Именно на него ругается консоль!)
export interface InputProblemStep extends BaseProblemStep {
    stepType: 'INPUT_PROBLEM';
}

// ТЕСТ (Варианты ответов)
export interface ChoiceProblemStep extends BaseProblemStep {
    stepType: 'CHOICE_PROBLEM';
    options: string[];
}

// Объединенный тип
export type AnyStep = TheoryStep | CodeProblemStep | InputProblemStep | ChoiceProblemStep;

// Интерфейс самого Урока
export interface FullLesson {
    id: number;
    title: string;
    orderIndex: number;
    steps: AnyStep[];
}