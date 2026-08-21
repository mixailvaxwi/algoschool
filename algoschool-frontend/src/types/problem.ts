export type ProblemType =
    | 'CHOICE_PROBLEM'
    | 'INPUT_PROBLEM'
    | 'CODE_PROBLEM'
    | 'NUMERIC_PROBLEM'
    | 'MATCHING_PROBLEM'
    | 'ORDERING_PROBLEM'
    | 'OPEN_ANSWER_PROBLEM';

export type ToleranceKind = 'ABSOLUTE' | 'RELATIVE';
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type Visibility = 'PRIVATE' | 'PUBLIC';

export const PROBLEM_TYPE_LABELS: Record<ProblemType, string> = {
    CHOICE_PROBLEM: '🔘 Тест (с вариантами)',
    INPUT_PROBLEM: '⌨️ Точный ввод ответа',
    CODE_PROBLEM: '💻 Программирование',
    NUMERIC_PROBLEM: '🔢 Числовой ответ',
    MATCHING_PROBLEM: '🔗 Соответствие',
    ORDERING_PROBLEM: '↕️ Упорядочивание',
    OPEN_ANSWER_PROBLEM: '📝 Развёрнутый ответ',
};

/** Проверяет человек, а не платформа: решения ждут преподавателя. */
export const MANUALLY_REVIEWED_TYPES: ProblemType[] = ['OPEN_ANSWER_PROBLEM'];

export const DIFFICULTY_LABELS: Record<Difficulty, string> = {
    EASY: 'Лёгкая',
    MEDIUM: 'Средняя',
    HARD: 'Сложная',
};

export const VISIBILITY_LABELS: Record<Visibility, string> = {
    PRIVATE: 'Только я',
    PUBLIC: 'Все преподаватели',
};

/** Задача банка, как её отдаёт /api/teacher/problems. */
export interface ProblemDto {
    id: number;
    problemType: ProblemType;
    title: string;
    description: string;
    difficulty: Difficulty | null;
    visibility: Visibility;
    maxScore: number;
    tags: string[];
    authorId: number;
    authorName: string;
    /** Правит и удаляет задачу только автор; чужую публичную можно лишь вставить в урок. */
    editable: boolean;
    /** В скольких уроках задача сейчас стоит. */
    usageCount: number;
    attemptedStudentsCount: number;
    successStudentsCount: number;
    updatedAt: string;

    options?: string[];
    correctOptionIndexes?: number[];
    isMultipleChoice?: boolean;
    correctAnswer?: string;
    correctValue?: number;
    tolerance?: number;
    toleranceKind?: ToleranceKind;
    leftItems?: string[];
    rightItems?: string[];
    orderedItems?: string[];
    reviewGuidelines?: string;
    timeLimitSec?: number;
    memoryLimitMb?: number;
    allowedLanguages?: string;
    ejudgeContestId?: number;
    ejudgeProblemId?: string;
}

/**
 * Состояние формы содержания задачи. Одно и то же и в банке, и в редакторе
 * урока: задача заводится обоими путями, и расходиться формы не должны.
 */
export interface ProblemContent {
    title: string;
    description: string;
    difficulty: '' | Difficulty;
    visibility: Visibility;
    maxScore: number;
    tags: string[];
    options: string[];
    correctOptionIndexes: number[];
    isMultipleChoice: boolean;
    correctAnswer: string;
    correctValue: number | '';
    tolerance: number;
    toleranceKind: ToleranceKind;
    /** Параллельные списки: leftItems[i] соответствует rightItems[i]. */
    leftItems: string[];
    rightItems: string[];
    /** Элементы в правильном порядке — перемешивает их сервер. */
    orderedItems: string[];
    reviewGuidelines: string;
    timeLimitSec: number;
    memoryLimitMb: number;
    allowedLanguages: string;
    ejudgeContestId: number | '';
    ejudgeProblemId: string;
}

export const emptyProblemContent = (): ProblemContent => ({
    title: '',
    description: '',
    difficulty: '',
    visibility: 'PRIVATE',
    maxScore: 1,
    tags: [],
    options: ['Вариант 1', 'Вариант 2'],
    correctOptionIndexes: [0],
    isMultipleChoice: false,
    correctAnswer: '',
    correctValue: '',
    tolerance: 0,
    toleranceKind: 'ABSOLUTE',
    leftItems: ['Слева 1', 'Слева 2'],
    rightItems: ['Справа 1', 'Справа 2'],
    orderedItems: ['Первый', 'Второй', 'Третий'],
    reviewGuidelines: '',
    timeLimitSec: 2,
    memoryLimitMb: 256,
    allowedLanguages: 'Java, Python, C++',
    ejudgeContestId: '',
    ejudgeProblemId: '',
});

/** Общая часть ProblemDto и StepDto: у шага поля задачи приезжают вместе с ним. */
interface ProblemContentSource {
    title?: string;
    description?: string;
    difficulty?: Difficulty | null;
    visibility?: Visibility | null;
    maxScore?: number;
    tags?: string[];
    options?: string[];
    correctOptionIndexes?: number[];
    isMultipleChoice?: boolean;
    correctAnswer?: string;
    correctValue?: number;
    tolerance?: number;
    toleranceKind?: ToleranceKind;
    leftItems?: string[];
    rightItems?: string[];
    orderedItems?: string[];
    reviewGuidelines?: string;
    timeLimitSec?: number;
    memoryLimitMb?: number;
    allowedLanguages?: string;
    ejudgeContestId?: number;
    ejudgeProblemId?: string;
}

export const problemContentFrom = (source: ProblemContentSource): ProblemContent => {
    const fallback = emptyProblemContent();
    return {
        title: source.title ?? '',
        description: source.description ?? '',
        difficulty: source.difficulty ?? '',
        visibility: source.visibility ?? 'PRIVATE',
        maxScore: source.maxScore ?? fallback.maxScore,
        tags: source.tags ?? [],
        options: source.options && source.options.length > 0 ? source.options : fallback.options,
        correctOptionIndexes:
            source.correctOptionIndexes && source.correctOptionIndexes.length > 0
                ? source.correctOptionIndexes
                : fallback.correctOptionIndexes,
        isMultipleChoice: source.isMultipleChoice ?? false,
        correctAnswer: source.correctAnswer ?? '',
        correctValue: source.correctValue ?? '',
        tolerance: source.tolerance ?? 0,
        toleranceKind: source.toleranceKind ?? 'ABSOLUTE',
        leftItems: source.leftItems && source.leftItems.length > 0 ? source.leftItems : fallback.leftItems,
        rightItems: source.rightItems && source.rightItems.length > 0 ? source.rightItems : fallback.rightItems,
        orderedItems:
            source.orderedItems && source.orderedItems.length > 0 ? source.orderedItems : fallback.orderedItems,
        reviewGuidelines: source.reviewGuidelines ?? '',
        timeLimitSec: source.timeLimitSec ?? fallback.timeLimitSec,
        memoryLimitMb: source.memoryLimitMb ?? fallback.memoryLimitMb,
        allowedLanguages: source.allowedLanguages ?? fallback.allowedLanguages,
        ejudgeContestId: source.ejudgeContestId ?? '',
        ejudgeProblemId: source.ejudgeProblemId ?? '',
    };
};

/**
 * Поля задачи для запроса. Пустые варианты ответа НЕ отфильтровываются: сервер
 * ждёт индексы правильных вариантов в том же списке, что прислали, и после
 * молчаливого выбрасывания пустой строки они указывали бы на соседние строки.
 * Пустой вариант сервер отклонит с внятным сообщением.
 */
export const problemContentPayload = (type: ProblemType, content: ProblemContent) => {
    const payload: Record<string, unknown> = {
        title: content.title.trim(),
        description: content.description,
        difficulty: content.difficulty || null,
        visibility: content.visibility,
        maxScore: content.maxScore,
        tags: content.tags,
    };

    if (type === 'INPUT_PROBLEM') {
        payload.correctAnswer = content.correctAnswer;
    }
    if (type === 'CHOICE_PROBLEM') {
        payload.options = content.options;
        payload.correctOptionIndexes = content.correctOptionIndexes;
        payload.isMultipleChoice = content.isMultipleChoice;
    }
    if (type === 'NUMERIC_PROBLEM') {
        payload.correctValue = content.correctValue === '' ? null : Number(content.correctValue);
        payload.tolerance = content.tolerance;
        payload.toleranceKind = content.toleranceKind;
    }
    if (type === 'MATCHING_PROBLEM') {
        payload.leftItems = content.leftItems;
        payload.rightItems = content.rightItems;
    }
    if (type === 'ORDERING_PROBLEM') {
        payload.orderedItems = content.orderedItems;
    }
    if (type === 'OPEN_ANSWER_PROBLEM') {
        payload.reviewGuidelines = content.reviewGuidelines;
    }
    if (type === 'CODE_PROBLEM') {
        payload.timeLimitSec = content.timeLimitSec;
        payload.memoryLimitMb = content.memoryLimitMb;
        payload.allowedLanguages = content.allowedLanguages;
        payload.ejudgeContestId = content.ejudgeContestId === '' ? null : Number(content.ejudgeContestId);
        payload.ejudgeProblemId = content.ejudgeProblemId;
    }

    return payload;
};
