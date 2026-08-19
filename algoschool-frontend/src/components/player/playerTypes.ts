export interface BaseStep {
    id: number;
    orderIndex: number;
    stepType: 'THEORY' | 'CODE_PROBLEM' | 'INPUT_PROBLEM' | 'CHOICE_PROBLEM';
}

export interface TheoryStep extends BaseStep {
    stepType: 'THEORY';
    content: string;
}

// Убраны difficulty и xpReward
export interface BaseProblemStep extends BaseStep {
    description: string;
}

export interface CodeProblemStep extends BaseProblemStep {
    stepType: 'CODE_PROBLEM';
    timeLimitSec: number;
    memoryLimitMb: number;
    allowedLanguages: string; // Если здесь тоже ожидается массив языков, замените на string[]
}

export interface InputProblemStep extends BaseProblemStep {
    stepType: 'INPUT_PROBLEM';
}

export interface ChoiceProblemStep extends BaseProblemStep {
    stepType: 'CHOICE_PROBLEM';
    isMultipleChoice: boolean;
    options: string[];
}

export type AnyStep = TheoryStep | CodeProblemStep | InputProblemStep | ChoiceProblemStep;

export interface FullLesson {
    id: number;
    title: string;
    orderIndex: number;
    steps: AnyStep[]; // ИСПРАВЛЕНО: заменено на массив (AnyStep[]), так как шагов в уроке обычно несколько
}