export type GradeItemKind = 'PROBLEM' | 'MANUAL';
export type GradePolicy = 'BEST' | 'LAST' | 'FIRST' | 'AVERAGE';

export const POLICY_LABELS: Record<GradePolicy, string> = {
    BEST: 'Лучшая попытка',
    LAST: 'Последняя попытка',
    FIRST: 'Первая попытка',
    AVERAGE: 'Среднее по попыткам',
};

/** Столбец журнала оценок. */
export interface GradeItemDto {
    id: number;
    kind: GradeItemKind;
    stepId: number | null;
    lessonId: number | null;
    title: string;
    maxScore: number;
    policy: GradePolicy;
    orderIndex: number;
    /** «Модуль 1 · Урок 2 · шаг 3» — где в курсе стоит задача. */
    usageHint: string | null;
}

/**
 * Клетка журнала. Поле называется manual, а не isManual: у is-полей приставку
 * по-разному обрезают Lombok и Jackson, и одно такое поле уже уезжало в JSON
 * под чужим именем.
 */
export interface GradeCellDto {
    score: number;
    manual: boolean;
    comment: string | null;
}

export interface GradebookRowDto {
    userId: number;
    username: string;
    name: string;
    /** Ключ — id элемента. Элементов без оценки в объекте нет: «не сдавал» ≠ «ноль». */
    scores: Record<string, GradeCellDto>;
    totalScore: number;
    percent: number;
}

export interface GradebookDto {
    courseId: number;
    courseTitle: string;
    items: GradeItemDto[];
    rows: GradebookRowDto[];
    totalMaxScore: number;
}

export interface MyGradeDto {
    itemId: number;
    kind: GradeItemKind;
    stepId: number | null;
    lessonId: number | null;
    title: string;
    maxScore: number;
    score: number | null;
    comment: string | null;
}

export interface MyGradesDto {
    courseId: number;
    courseTitle: string;
    grades: MyGradeDto[];
    totalScore: number;
    totalMaxScore: number;
    percent: number;
}
