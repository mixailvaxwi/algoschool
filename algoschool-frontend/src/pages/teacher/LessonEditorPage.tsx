import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { ProblemFields } from '../../components/teacher/ProblemFields';
import { ProblemPickerModal } from '../../components/teacher/ProblemPickerModal';
import {
    PROBLEM_TYPE_LABELS,
    emptyProblemContent,
    problemContentFrom,
    problemContentPayload,
    type Difficulty,
    type ProblemContent,
    type ProblemDto,
    type ProblemType,
    type Visibility,
} from '../../types/problem';
import { PlusCircle, Save, ArrowLeft, Trash2, Hash, Pencil, X, BookOpen, Library, Link2Off, Layers } from 'lucide-react';

type StepType = 'THEORY' | ProblemType;

interface StepDto {
    id: number;
    stepType: StepType;
    orderIndex: number;
    content?: string;

    problemId?: number;
    title?: string;
    /** В скольких уроках стоит эта задача: правка затрагивает их все. */
    problemUsageCount?: number;
    /** Чужую публичную задачу можно только переставить, но не переписать. */
    problemEditable?: boolean;
    visibility?: Visibility;
    difficulty?: Difficulty | null;
    maxScore?: number;
    tags?: string[];
    description?: string;
    options?: string[];
    correctOptionIndexes?: number[];
    isMultipleChoice?: boolean;
    correctAnswer?: string;
    timeLimitSec?: number;
    memoryLimitMb?: number;
    allowedLanguages?: string;
    ejudgeContestId?: number;
    ejudgeProblemId?: string;
}

const STEP_TYPE_LABELS: Record<StepType, string> = {
    THEORY: '📖 Теория',
    ...PROBLEM_TYPE_LABELS,
};

/** Задача, на которую ссылается редактируемый или создаваемый шаг. */
interface LinkedProblem {
    id: number;
    title: string;
    usageCount: number;
    editable: boolean;
    /** true — задача выбрана из банка, содержание отсюда не заводится. */
    fromBank: boolean;
}

export const LessonEditorPage = () => {
    const { courseId, lessonId } = useParams();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    const [steps, setSteps] = useState<StepDto[]>([]);
    const [isLoadingSteps, setIsLoadingSteps] = useState(true);

    // null => создаём новый шаг, иначе — id редактируемого шага
    const [editingStepId, setEditingStepId] = useState<number | null>(null);

    const [stepType, setStepType] = useState<StepType>('THEORY');
    const [orderIndex, setOrderIndex] = useState<number>(1);
    const [content, setContent] = useState('');
    const [problem, setProblem] = useState<ProblemContent>(emptyProblemContent());
    const [linked, setLinked] = useState<LinkedProblem | null>(null);
    const [isPickerOpen, setIsPickerOpen] = useState(false);

    const fetchSteps = async () => {
        setIsLoadingSteps(true);
        try {
            const response = await apiClient.get<StepDto[]>(`/teacher/courses/${courseId}/lessons/${lessonId}/steps`);
            setSteps(response.data.sort((a, b) => a.orderIndex - b.orderIndex));
        } catch (error) {
            console.error('Ошибка загрузки шагов', error);
        } finally {
            setIsLoadingSteps(false);
        }
    };

    useEffect(() => {
        fetchSteps();
    }, [courseId, lessonId]);

    const resetForm = (nextOrderIndex: number) => {
        setEditingStepId(null);
        setStepType('THEORY');
        setOrderIndex(nextOrderIndex);
        setContent('');
        setProblem(emptyProblemContent());
        setLinked(null);
    };

    const startEditingStep = (step: StepDto) => {
        setEditingStepId(step.id);
        setStepType(step.stepType);
        setOrderIndex(step.orderIndex);
        setContent(step.content ?? '');
        setProblem(problemContentFrom(step));
        setLinked(
            step.problemId
                ? {
                      id: step.problemId,
                      title: step.title ?? '',
                      usageCount: step.problemUsageCount ?? 1,
                      editable: step.problemEditable ?? true,
                      fromBank: false,
                  }
                : null,
        );

        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const cancelEditing = () => resetForm(steps.length + 1);

    const handlePickProblem = (picked: ProblemDto) => {
        setStepType(picked.problemType);
        setProblem(problemContentFrom(picked));
        setLinked({
            id: picked.id,
            title: picked.title,
            usageCount: picked.usageCount,
            editable: picked.editable,
            fromBank: true,
        });
        setIsPickerOpen(false);
    };

    const handleDeleteStep = async (step: StepDto) => {
        const question =
            step.stepType === 'THEORY'
                ? `Удалить шаг №${step.orderIndex}? Это действие необратимо.`
                : `Снять задачу «${step.title}» с урока? В банке задача останется.`;
        if (!window.confirm(question)) return;

        try {
            await apiClient.delete(`/teacher/courses/${courseId}/lessons/${lessonId}/steps/${step.id}`);
            setSteps(steps.filter((s) => s.id !== step.id));
            if (editingStepId === step.id) cancelEditing();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось удалить шаг');
        }
    };

    const buildPayload = () => {
        if (stepType === 'THEORY') {
            return { stepType, orderIndex, content };
        }

        // Задача из банка ставится по ссылке: её содержание живёт в банке и
        // отсюда не пересоздаётся.
        if (linked?.fromBank) {
            return { stepType, orderIndex, problemId: linked.id };
        }

        // Чужая задача, уже стоящая в уроке: менять можно только позицию.
        if (linked && !linked.editable) {
            return { stepType, orderIndex, problemId: linked.id };
        }

        return { stepType, orderIndex, ...problemContentPayload(stepType, problem) };
    };

    const handleSaveStep = async () => {
        setIsLoading(true);
        try {
            const payload = buildPayload();
            if (editingStepId) {
                const response = await apiClient.put<StepDto>(
                    `/teacher/courses/${courseId}/lessons/${lessonId}/steps/${editingStepId}`,
                    payload,
                );
                setSteps(
                    steps
                        .map((s) => (s.id === editingStepId ? response.data : s))
                        .sort((a, b) => a.orderIndex - b.orderIndex),
                );
                resetForm(steps.length + 1);
            } else {
                const response = await apiClient.post<StepDto>(
                    `/teacher/courses/${courseId}/lessons/${lessonId}/steps`,
                    payload,
                );
                setSteps([...steps, response.data].sort((a, b) => a.orderIndex - b.orderIndex));

                // Авто-инкремент для следующего шага, остальное поле очищаем
                resetForm(orderIndex + 1);
            }
        } catch (error: any) {
            alert(error.response?.data?.message || 'Ошибка при сохранении');
        } finally {
            setIsLoading(false);
        }
    };

    const isProblemStep = stepType !== 'THEORY';
    const contentEditable = !linked || linked.editable;

    return (
        <div className="max-w-3xl mx-auto py-8">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-slate-500 mb-6 hover:text-slate-800 transition-colors"
            >
                <ArrowLeft size={18} /> К структуре курса
            </button>

            {/* Список существующих шагов */}
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 mb-6">
                <h2 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                    <BookOpen className="text-blue-600" size={20} /> Шаги урока
                </h2>

                {isLoadingSteps ? (
                    <div className="text-slate-400 text-sm py-4">Загрузка шагов...</div>
                ) : steps.length === 0 ? (
                    <div className="text-slate-400 text-sm italic py-4">Шагов пока нет. Создайте первый ниже.</div>
                ) : (
                    <div className="space-y-2">
                        {steps.map((step) => (
                            <div
                                key={step.id}
                                className={`flex items-center justify-between p-3 rounded-xl border transition-colors ${
                                    editingStepId === step.id
                                        ? 'border-blue-400 bg-blue-50'
                                        : 'border-slate-200 hover:bg-slate-50'
                                }`}
                            >
                                <div className="flex items-center gap-3 min-w-0">
                                    <span className="text-slate-400 font-mono text-sm w-6 text-right">{step.orderIndex}</span>
                                    <span className="text-sm font-medium text-slate-700 shrink-0">
                                        {STEP_TYPE_LABELS[step.stepType]}
                                    </span>
                                    <span className="text-sm text-slate-500 truncate">
                                        {step.stepType === 'THEORY' ? step.content : step.title}
                                    </span>
                                    {(step.problemUsageCount ?? 0) > 1 && (
                                        <span
                                            className="shrink-0 text-xs flex items-center gap-1 bg-amber-100 text-amber-700 px-2 py-0.5 rounded-md"
                                            title="Задача стоит в нескольких уроках — правка изменит её везде"
                                        >
                                            <Layers size={12} /> {step.problemUsageCount}
                                        </span>
                                    )}
                                </div>
                                <div className="flex items-center gap-1 shrink-0 ml-3">
                                    <button
                                        onClick={() => startEditingStep(step)}
                                        className="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-100 rounded-lg transition-colors"
                                        title="Редактировать шаг"
                                    >
                                        <Pencil size={16} />
                                    </button>
                                    <button
                                        onClick={() => handleDeleteStep(step)}
                                        className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-100 rounded-lg transition-colors"
                                        title={step.stepType === 'THEORY' ? 'Удалить шаг' : 'Снять задачу с урока'}
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200">
                <div className="flex items-center justify-between mb-8">
                    <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                        <PlusCircle className="text-blue-600" />
                        {editingStepId ? `Редактирование шага №${orderIndex}` : 'Создание шага'}
                    </h1>
                    {editingStepId && (
                        <button
                            onClick={cancelEditing}
                            className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800 transition-colors"
                        >
                            <X size={16} /> Отменить редактирование
                        </button>
                    )}
                </div>

                {/* Базовые настройки */}
                <div className="grid grid-cols-2 gap-6 mb-8">
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Тип контента</label>
                        <select
                            value={stepType}
                            disabled={!!editingStepId || linked?.fromBank}
                            onChange={(e) => setStepType(e.target.value as StepType)}
                            className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-60 disabled:cursor-not-allowed"
                        >
                            {(Object.keys(STEP_TYPE_LABELS) as StepType[]).map((value) => (
                                <option key={value} value={value}>{STEP_TYPE_LABELS[value]}</option>
                            ))}
                        </select>
                        {editingStepId && (
                            <p className="text-xs text-slate-400 mt-1">
                                Тип существующего шага изменить нельзя — удалите шаг и создайте новый.
                            </p>
                        )}
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Позиция в уроке (№)</label>
                        <div className="relative">
                            <Hash className="absolute left-3 top-3.5 text-slate-400" size={18} />
                            <input
                                type="number"
                                min="1"
                                value={orderIndex}
                                onChange={(e) => setOrderIndex(Number(e.target.value))}
                                className="w-full p-3 pl-10 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>
                </div>

                {stepType === 'THEORY' ? (
                    <div className="mb-6">
                        <label className="block text-sm font-semibold text-slate-700 mb-2">
                            Текст теории (HTML/Markdown)
                        </label>
                        <textarea
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            className="w-full h-48 p-4 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 resize-y"
                            placeholder="Введите обучающий материал..."
                        />
                    </div>
                ) : (
                    <>
                        {!editingStepId && !linked && (
                            <div className="mb-6 flex items-center justify-between gap-4 p-4 bg-slate-50 border border-slate-200 rounded-xl">
                                <p className="text-sm text-slate-600">
                                    Задача будет создана в вашем банке и поставлена в этот урок. Можно вместо этого
                                    взять уже готовую.
                                </p>
                                <button
                                    type="button"
                                    onClick={() => setIsPickerOpen(true)}
                                    className="shrink-0 flex items-center gap-2 px-4 py-2 border border-blue-200 text-blue-700 hover:bg-blue-50 rounded-xl font-medium transition-colors"
                                >
                                    <Library size={16} /> Выбрать из банка
                                </button>
                            </div>
                        )}

                        {linked?.fromBank && (
                            <div className="mb-6 flex items-center justify-between gap-4 p-4 bg-blue-50 border border-blue-200 rounded-xl">
                                <div className="min-w-0">
                                    <p className="text-sm font-semibold text-blue-900 truncate">{linked.title}</p>
                                    <p className="text-xs text-blue-700 mt-0.5">
                                        Задача из банка, уже стоит в уроках: {linked.usageCount}. Условие и ответ
                                        меняются в банке.
                                    </p>
                                </div>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setLinked(null);
                                        setProblem(emptyProblemContent());
                                    }}
                                    className="shrink-0 flex items-center gap-2 px-4 py-2 text-slate-500 hover:text-slate-800 transition-colors"
                                >
                                    <Link2Off size={16} /> Отвязать
                                </button>
                            </div>
                        )}

                        {linked && !linked.fromBank && linked.usageCount > 1 && linked.editable && (
                            <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-xl text-sm text-amber-800">
                                Задача стоит в {linked.usageCount} уроках — правка изменит её во всех сразу.
                            </div>
                        )}

                        {linked && !linked.editable && (
                            <div className="mb-6 p-4 bg-slate-100 border border-slate-200 rounded-xl text-sm text-slate-600">
                                Задача из банка другого преподавателя: её содержание отсюда не изменить, можно
                                поменять только позицию шага в уроке.
                            </div>
                        )}

                        <ProblemFields
                            type={stepType}
                            value={problem}
                            onChange={setProblem}
                            disabled={linked?.fromBank || !contentEditable}
                        />
                    </>
                )}

                <div className="mt-8 pt-6 border-t border-slate-100 flex justify-end">
                    <button
                        onClick={handleSaveStep}
                        disabled={isLoading || (isProblemStep && !linked && !problem.description.trim())}
                        className="bg-blue-600 text-white px-10 py-3 rounded-xl font-bold hover:bg-blue-700 transition-all disabled:opacity-50 flex items-center gap-2"
                    >
                        <Save size={20} />
                        {isLoading ? 'Сохранение...' : editingStepId ? 'Сохранить изменения' : 'Создать шаг'}
                    </button>
                </div>
            </div>

            {isPickerOpen && (
                <ProblemPickerModal onPick={handlePickProblem} onClose={() => setIsPickerOpen(false)} />
            )}
        </div>
    );
};
