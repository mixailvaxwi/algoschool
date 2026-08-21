import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '../../api/axios';
import {
    DIFFICULTY_LABELS,
    PROBLEM_TYPE_LABELS,
    type ProblemDto,
    type ProblemType,
} from '../../types/problem';
import { Search, X, Library, Globe, Lock } from 'lucide-react';

interface Props {
    onPick: (problem: ProblemDto) => void;
    onClose: () => void;
}

/**
 * Выбор готовой задачи из банка для вставки в урок (UC-T-22).
 * Показываются свои задачи и чужие публичные.
 */
export const ProblemPickerModal = ({ onPick, onClose }: Props) => {
    const [problems, setProblems] = useState<ProblemDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [query, setQuery] = useState('');
    const [type, setType] = useState<'' | ProblemType>('');

    const fetchProblems = useCallback(async (q: string, problemType: '' | ProblemType) => {
        setIsLoading(true);
        try {
            const response = await apiClient.get<ProblemDto[]>('/teacher/problems', {
                params: { q: q || undefined, type: problemType || undefined },
            });
            setProblems(response.data);
        } catch (error) {
            console.error('Ошибка загрузки банка задач', error);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchProblems('', '');
    }, [fetchProblems]);

    return (
        <div className="fixed inset-0 bg-slate-900/50 flex items-start justify-center p-4 z-50 overflow-y-auto">
            <div className="bg-white rounded-2xl shadow-xl max-w-3xl w-full p-6 my-8">
                <div className="flex items-center justify-between mb-5">
                    <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
                        <Library className="text-blue-600" size={20} /> Задача из банка
                    </h2>
                    <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
                        <X size={20} />
                    </button>
                </div>

                <form
                    onSubmit={(e) => {
                        e.preventDefault();
                        fetchProblems(query, type);
                    }}
                    className="flex gap-2 mb-5"
                >
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-3 text-slate-400" size={18} />
                        <input
                            type="text"
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                            placeholder="Название или условие"
                            className="w-full pl-10 pr-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <select
                        value={type}
                        onChange={(e) => setType(e.target.value as '' | ProblemType)}
                        className="px-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                    >
                        <option value="">Любой тип</option>
                        {(Object.keys(PROBLEM_TYPE_LABELS) as ProblemType[]).map((value) => (
                            <option key={value} value={value}>{PROBLEM_TYPE_LABELS[value]}</option>
                        ))}
                    </select>
                    <button
                        type="submit"
                        className="px-5 py-2 bg-slate-900 hover:bg-slate-800 text-white font-medium rounded-xl transition-colors"
                    >
                        Найти
                    </button>
                </form>

                {isLoading ? (
                    <div className="text-slate-400 text-sm py-8 text-center">Загрузка...</div>
                ) : problems.length === 0 ? (
                    <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                        Подходящих задач не нашлось.
                    </div>
                ) : (
                    <div className="space-y-2 max-h-[50vh] overflow-y-auto">
                        {problems.map((problem) => (
                            <button
                                key={problem.id}
                                onClick={() => onPick(problem)}
                                className="w-full text-left p-3 border border-slate-200 rounded-xl hover:border-blue-400 hover:bg-blue-50/50 transition-colors"
                            >
                                <div className="flex items-center gap-2 flex-wrap">
                                    <span className="text-sm text-slate-500">{PROBLEM_TYPE_LABELS[problem.problemType]}</span>
                                    <span className="font-semibold text-slate-800">{problem.title}</span>
                                    {problem.difficulty && (
                                        <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded-md">
                                            {DIFFICULTY_LABELS[problem.difficulty]}
                                        </span>
                                    )}
                                    <span className="text-xs text-slate-400 flex items-center gap-1">
                                        {problem.visibility === 'PUBLIC' ? <Globe size={12} /> : <Lock size={12} />}
                                        {problem.editable ? 'моя' : problem.authorName}
                                    </span>
                                </div>
                                <p className="text-sm text-slate-500 mt-1 line-clamp-1">{problem.description}</p>
                                <p className="text-xs text-slate-400 mt-1">
                                    уже стоит в уроках: {problem.usageCount}
                                    {problem.tags.length > 0 && ` · ${problem.tags.map((t) => `#${t}`).join(' ')}`}
                                </p>
                            </button>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};
