import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '../../api/axios';
import { ProblemFields } from '../../components/teacher/ProblemFields';
import {
    DIFFICULTY_LABELS,
    PROBLEM_TYPE_LABELS,
    VISIBILITY_LABELS,
    emptyProblemContent,
    problemContentFrom,
    problemContentPayload,
    type Difficulty,
    type ProblemContent,
    type ProblemDto,
    type ProblemType,
} from '../../types/problem';
import { Library, Search, Plus, Pencil, Trash2, X, Save, Users, Lock, Globe } from 'lucide-react';

interface Filters {
    q: string;
    type: '' | ProblemType;
    difficulty: '' | Difficulty;
    tag: string;
    onlyMine: boolean;
}

const EMPTY_FILTERS: Filters = { q: '', type: '', difficulty: '', tag: '', onlyMine: false };

/**
 * Банк задач (UC-T-20…22).
 * <p>
 * Задача живёт здесь, а не в уроке: одну и ту же можно поставить в несколько
 * уроков, и статистика по ней остаётся общей.
 */
export const TeacherProblemsPage = () => {
    const [problems, setProblems] = useState<ProblemDto[]>([]);
    const [tags, setTags] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [filters, setFilters] = useState<Filters>(EMPTY_FILTERS);
    const [applied, setApplied] = useState<Filters>(EMPTY_FILTERS);
    const [editorState, setEditorState] = useState<{ problem: ProblemDto | null } | null>(null);

    const fetchProblems = useCallback(async (current: Filters) => {
        setIsLoading(true);
        try {
            const response = await apiClient.get<ProblemDto[]>('/teacher/problems', {
                params: {
                    q: current.q || undefined,
                    type: current.type || undefined,
                    difficulty: current.difficulty || undefined,
                    tag: current.tag || undefined,
                    onlyMine: current.onlyMine || undefined,
                },
            });
            setProblems(response.data);
        } catch (error) {
            console.error('Ошибка загрузки банка задач', error);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchProblems(applied);
    }, [fetchProblems, applied]);

    useEffect(() => {
        apiClient
            .get<string[]>('/teacher/problems/tags')
            .then((response) => setTags(response.data))
            .catch(() => setTags([]));
    }, [editorState]);

    const handleDelete = async (problem: ProblemDto) => {
        if (!window.confirm(`Удалить задачу «${problem.title}»? Это действие необратимо.`)) return;

        try {
            await apiClient.delete(`/teacher/problems/${problem.id}`);
            setProblems((prev) => prev.filter((p) => p.id !== problem.id));
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось удалить задачу');
        }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <Library className="text-blue-600" size={24} />
                    <h1 className="text-2xl font-bold text-slate-800">Банк задач</h1>
                </div>
                <button
                    onClick={() => setEditorState({ problem: null })}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-medium transition-colors"
                >
                    <Plus size={18} /> Создать задачу
                </button>
            </div>

            <form
                onSubmit={(e) => {
                    e.preventDefault();
                    setApplied(filters);
                }}
                className="grid grid-cols-1 md:grid-cols-5 gap-3 mb-6"
            >
                <div className="relative md:col-span-2">
                    <Search className="absolute left-3 top-3 text-slate-400" size={18} />
                    <input
                        type="text"
                        value={filters.q}
                        onChange={(e) => setFilters({ ...filters, q: e.target.value })}
                        placeholder="Название или условие"
                        className="w-full pl-10 pr-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <select
                    value={filters.type}
                    onChange={(e) => setFilters({ ...filters, type: e.target.value as '' | ProblemType })}
                    className="px-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                >
                    <option value="">Любой тип</option>
                    {(Object.keys(PROBLEM_TYPE_LABELS) as ProblemType[]).map((type) => (
                        <option key={type} value={type}>{PROBLEM_TYPE_LABELS[type]}</option>
                    ))}
                </select>
                <select
                    value={filters.difficulty}
                    onChange={(e) => setFilters({ ...filters, difficulty: e.target.value as '' | Difficulty })}
                    className="px-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                >
                    <option value="">Любая сложность</option>
                    {(Object.keys(DIFFICULTY_LABELS) as Difficulty[]).map((level) => (
                        <option key={level} value={level}>{DIFFICULTY_LABELS[level]}</option>
                    ))}
                </select>
                <select
                    value={filters.tag}
                    onChange={(e) => setFilters({ ...filters, tag: e.target.value })}
                    className="px-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                >
                    <option value="">Любой тег</option>
                    {tags.map((tag) => (
                        <option key={tag} value={tag}>{tag}</option>
                    ))}
                </select>

                <label className="flex items-center gap-2 text-sm text-slate-600 md:col-span-2">
                    <input
                        type="checkbox"
                        checked={filters.onlyMine}
                        onChange={(e) => setFilters({ ...filters, onlyMine: e.target.checked })}
                        className="w-4 h-4 rounded text-blue-600 focus:ring-blue-500"
                    />
                    Только мои задачи
                </label>
                <div className="md:col-span-3 flex gap-2 justify-end">
                    <button
                        type="button"
                        onClick={() => {
                            setFilters(EMPTY_FILTERS);
                            setApplied(EMPTY_FILTERS);
                        }}
                        className="px-4 py-2 text-slate-500 hover:text-slate-800 transition-colors"
                    >
                        Сбросить
                    </button>
                    <button
                        type="submit"
                        className="px-5 py-2 bg-slate-900 hover:bg-slate-800 text-white font-medium rounded-xl transition-colors"
                    >
                        Найти
                    </button>
                </div>
            </form>

            {isLoading ? (
                <div className="text-slate-400 text-sm py-8 text-center">Загрузка...</div>
            ) : problems.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    Задач не нашлось. Создайте первую — и её можно будет ставить в любые свои уроки.
                </div>
            ) : (
                <div className="space-y-3">
                    {problems.map((problem) => (
                        <ProblemRow
                            key={problem.id}
                            problem={problem}
                            onEdit={() => setEditorState({ problem })}
                            onDelete={() => handleDelete(problem)}
                        />
                    ))}
                </div>
            )}

            {editorState && (
                <ProblemEditorModal
                    problem={editorState.problem}
                    onClose={() => setEditorState(null)}
                    onSaved={(saved) => {
                        setProblems((prev) => {
                            const exists = prev.some((p) => p.id === saved.id);
                            return exists ? prev.map((p) => (p.id === saved.id ? saved : p)) : [saved, ...prev];
                        });
                        setEditorState(null);
                    }}
                />
            )}
        </div>
    );
};

const ProblemRow = ({
    problem,
    onEdit,
    onDelete,
}: {
    problem: ProblemDto;
    onEdit: () => void;
    onDelete: () => void;
}) => (
    <div className="flex items-start justify-between gap-4 p-4 border border-slate-200 rounded-xl hover:bg-slate-50 transition-colors">
        <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
                <span className="text-sm font-medium text-slate-500">{PROBLEM_TYPE_LABELS[problem.problemType]}</span>
                <span className="font-semibold text-slate-800">{problem.title}</span>
                {problem.difficulty && (
                    <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded-md">
                        {DIFFICULTY_LABELS[problem.difficulty]}
                    </span>
                )}
                <span
                    className="text-xs flex items-center gap-1 text-slate-500"
                    title={VISIBILITY_LABELS[problem.visibility]}
                >
                    {problem.visibility === 'PUBLIC' ? <Globe size={13} /> : <Lock size={13} />}
                    {VISIBILITY_LABELS[problem.visibility]}
                </span>
            </div>

            <p className="text-sm text-slate-500 mt-1 line-clamp-2">{problem.description}</p>

            <div className="flex items-center gap-4 mt-2 text-xs text-slate-400 flex-wrap">
                <span className="flex items-center gap-1">
                    <Users size={13} /> в уроках: {problem.usageCount}
                </span>
                <span>
                    решили {problem.successStudentsCount} из {problem.attemptedStudentsCount} пробовавших
                </span>
                {!problem.editable && <span>автор: {problem.authorName}</span>}
                {problem.tags.map((tag) => (
                    <span key={tag} className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md">#{tag}</span>
                ))}
            </div>
        </div>

        <div className="flex items-center gap-1 shrink-0">
            <button
                onClick={onEdit}
                className="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
                title={problem.editable ? 'Редактировать задачу' : 'Посмотреть задачу'}
            >
                <Pencil size={16} />
            </button>
            {problem.editable && (
                <button
                    onClick={onDelete}
                    className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Удалить задачу"
                >
                    <Trash2 size={16} />
                </button>
            )}
        </div>
    </div>
);

const ProblemEditorModal = ({
    problem,
    onClose,
    onSaved,
}: {
    problem: ProblemDto | null;
    onClose: () => void;
    onSaved: (saved: ProblemDto) => void;
}) => {
    const [type, setType] = useState<ProblemType>(problem?.problemType ?? 'CHOICE_PROBLEM');
    const [content, setContent] = useState<ProblemContent>(emptyProblemContent());
    const [isLoading, setIsLoading] = useState(problem !== null);
    const [isSaving, setIsSaving] = useState(false);
    const [editable, setEditable] = useState(true);

    useEffect(() => {
        if (!problem) return;

        // В списке правильных ответов нет — карточку задачи запрашиваем отдельно.
        apiClient
            .get<ProblemDto>(`/teacher/problems/${problem.id}`)
            .then((response) => {
                setType(response.data.problemType);
                setContent(problemContentFrom(response.data));
                setEditable(response.data.editable);
            })
            .catch(() => alert('Не удалось загрузить задачу'))
            .finally(() => setIsLoading(false));
    }, [problem]);

    const handleSave = async () => {
        setIsSaving(true);
        try {
            const payload = { problemType: type, ...problemContentPayload(type, content) };
            const response = problem
                ? await apiClient.put<ProblemDto>(`/teacher/problems/${problem.id}`, payload)
                : await apiClient.post<ProblemDto>('/teacher/problems', payload);
            onSaved(response.data);
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось сохранить задачу');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-slate-900/50 flex items-start justify-center p-4 z-50 overflow-y-auto">
            <div className="bg-white rounded-2xl shadow-xl max-w-3xl w-full p-6 my-8">
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-lg font-bold text-slate-800">
                        {problem ? (editable ? 'Редактирование задачи' : 'Задача другого преподавателя') : 'Новая задача'}
                    </h2>
                    <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
                        <X size={20} />
                    </button>
                </div>

                {isLoading ? (
                    <div className="text-slate-400 text-sm py-8 text-center">Загрузка...</div>
                ) : (
                    <>
                        {problem && problem.usageCount > 1 && editable && (
                            <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-xl text-sm text-amber-800">
                                Задача стоит в {problem.usageCount} уроках — правка изменит её во всех сразу.
                            </div>
                        )}

                        <div className="mb-6">
                            <label className="block text-sm font-semibold text-slate-700 mb-2">Тип задачи</label>
                            <select
                                value={type}
                                disabled={problem !== null}
                                onChange={(e) => setType(e.target.value as ProblemType)}
                                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-60 disabled:cursor-not-allowed"
                            >
                                {(Object.keys(PROBLEM_TYPE_LABELS) as ProblemType[]).map((value) => (
                                    <option key={value} value={value}>{PROBLEM_TYPE_LABELS[value]}</option>
                                ))}
                            </select>
                            {problem && (
                                <p className="text-xs text-slate-400 mt-1">
                                    Тип существующей задачи изменить нельзя — создайте новую задачу нужного типа.
                                </p>
                            )}
                        </div>

                        <ProblemFields type={type} value={content} onChange={setContent} disabled={!editable} />

                        <div className="mt-8 pt-6 border-t border-slate-100 flex justify-end gap-3">
                            <button onClick={onClose} className="px-5 py-2.5 text-slate-500 hover:text-slate-800 transition-colors">
                                {editable ? 'Отмена' : 'Закрыть'}
                            </button>
                            {editable && (
                                <button
                                    onClick={handleSave}
                                    disabled={isSaving}
                                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-8 py-2.5 rounded-xl font-bold transition-colors disabled:opacity-50"
                                >
                                    <Save size={18} /> {isSaving ? 'Сохранение...' : 'Сохранить'}
                                </button>
                            )}
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};
