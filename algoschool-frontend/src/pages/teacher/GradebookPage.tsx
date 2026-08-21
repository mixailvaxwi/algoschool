import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import {
    POLICY_LABELS,
    type GradebookDto,
    type GradeItemDto,
    type GradePolicy,
} from '../../types/grade';
import { ArrowLeft, ClipboardList, Download, Plus, Settings2, X, Save, Trash2, Pencil } from 'lucide-react';

/** Журнал оценок курса: строки — студенты, столбцы — оцениваемые элементы. */
export const GradebookPage = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();

    const [gradebook, setGradebook] = useState<GradebookDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [editingItem, setEditingItem] = useState<GradeItemDto | null>(null);
    const [isCreatingItem, setIsCreatingItem] = useState(false);
    const [editingCell, setEditingCell] = useState<{ item: GradeItemDto; userId: number; name: string } | null>(null);

    const fetchGradebook = useCallback(async () => {
        setIsLoading(true);
        try {
            const response = await apiClient.get<GradebookDto>(`/teacher/courses/${courseId}/gradebook`);
            setGradebook(response.data);
        } catch (error) {
            console.error('Ошибка загрузки журнала', error);
        } finally {
            setIsLoading(false);
        }
    }, [courseId]);

    useEffect(() => {
        fetchGradebook();
    }, [fetchGradebook]);

    const handleExport = async () => {
        try {
            const response = await apiClient.get(`/teacher/courses/${courseId}/gradebook/export`, {
                responseType: 'blob',
            });
            const url = URL.createObjectURL(response.data as Blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `gradebook-${courseId}.csv`;
            link.click();
            URL.revokeObjectURL(url);
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось выгрузить журнал');
        }
    };

    const handleDeleteItem = async (item: GradeItemDto) => {
        if (!window.confirm(`Удалить столбец «${item.title}» вместе с выставленными баллами?`)) return;
        try {
            await apiClient.delete(`/teacher/grade-items/${item.id}`);
            fetchGradebook();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось удалить столбец');
        }
    };

    if (isLoading) {
        return <div className="text-slate-400 text-sm py-8 text-center">Загрузка журнала...</div>;
    }
    if (!gradebook) {
        return <div className="text-slate-500 py-8 text-center">Журнал недоступен.</div>;
    }

    return (
        <div>
            <div className="flex items-center gap-4 mb-6">
                <button
                    onClick={() => navigate(`/teacher/courses/${courseId}`)}
                    className="p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700 rounded-lg transition-colors"
                    title="Назад к курсу"
                >
                    <ArrowLeft size={24} />
                </button>
                <div>
                    <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                        <ClipboardList className="text-blue-600" size={22} /> Журнал оценок
                    </h1>
                    <p className="text-slate-500 text-sm">{gradebook.courseTitle}</p>
                </div>
                <div className="ml-auto flex gap-2">
                    <button
                        onClick={() => setIsCreatingItem(true)}
                        className="flex items-center gap-2 px-4 py-2 border border-slate-200 hover:bg-slate-50 rounded-xl font-medium transition-colors"
                    >
                        <Plus size={16} /> Ручной столбец
                    </button>
                    <button
                        onClick={handleExport}
                        className="flex items-center gap-2 bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-xl font-medium transition-colors"
                    >
                        <Download size={16} /> Выгрузить CSV
                    </button>
                </div>
            </div>

            {gradebook.items.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    Оценивать пока нечего: в курсе нет ни задач, ни ручных столбцов.
                </div>
            ) : gradebook.rows.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    На курс ещё никто не записан.
                </div>
            ) : (
                <div className="overflow-x-auto border border-slate-200 rounded-xl">
                    <table className="text-sm min-w-full">
                        <thead>
                            <tr className="bg-slate-50 text-left text-slate-500 border-b border-slate-200">
                                <th className="px-4 py-3 font-semibold sticky left-0 bg-slate-50 z-10">Студент</th>
                                {gradebook.items.map((item) => (
                                    <th key={item.id} className="px-3 py-3 font-semibold whitespace-nowrap align-bottom">
                                        <div className="flex items-center gap-1">
                                            <span className="max-w-[10rem] truncate" title={item.title}>{item.title}</span>
                                            <button
                                                onClick={() => setEditingItem(item)}
                                                className="p-1 text-slate-300 hover:text-blue-600 transition-colors"
                                                title="Настроить столбец"
                                            >
                                                <Settings2 size={13} />
                                            </button>
                                            {item.kind === 'MANUAL' && (
                                                <button
                                                    onClick={() => handleDeleteItem(item)}
                                                    className="p-1 text-slate-300 hover:text-red-600 transition-colors"
                                                    title="Удалить столбец"
                                                >
                                                    <Trash2 size={13} />
                                                </button>
                                            )}
                                        </div>
                                        <div className="text-xs font-normal text-slate-400">
                                            {item.maxScore} б. · {item.kind === 'MANUAL' ? 'вручную' : POLICY_LABELS[item.policy]}
                                        </div>
                                        {item.usageHint && (
                                            <div className="text-xs font-normal text-slate-300">{item.usageHint}</div>
                                        )}
                                    </th>
                                ))}
                                <th className="px-4 py-3 font-semibold whitespace-nowrap">Итого</th>
                            </tr>
                        </thead>
                        <tbody>
                            {gradebook.rows.map((row) => (
                                <tr key={row.userId} className="border-b border-slate-100 last:border-0">
                                    <td className="px-4 py-2 font-medium text-slate-800 sticky left-0 bg-white z-10 whitespace-nowrap">
                                        {row.name}
                                        <span className="text-xs text-slate-400 ml-2">{row.username}</span>
                                    </td>
                                    {gradebook.items.map((item) => {
                                        const cell = row.scores[String(item.id)];
                                        return (
                                            <td key={item.id} className="px-3 py-2">
                                                <button
                                                    onClick={() => setEditingCell({ item, userId: row.userId, name: row.name })}
                                                    title={cell?.comment ?? 'Поставить балл вручную'}
                                                    className={`w-full text-left px-2 py-1 rounded-lg tabular-nums transition-colors ${
                                                        cell === undefined
                                                            ? 'text-slate-300 hover:bg-slate-50'
                                                            : cell.manual
                                                              ? 'bg-amber-50 text-amber-800 hover:bg-amber-100'
                                                              : 'text-slate-700 hover:bg-slate-50'
                                                    }`}
                                                >
                                                    {cell === undefined ? '—' : cell.score}
                                                    {cell?.manual && <Pencil size={11} className="inline ml-1 mb-0.5" />}
                                                </button>
                                            </td>
                                        );
                                    })}
                                    <td className="px-4 py-2 font-semibold text-slate-800 whitespace-nowrap tabular-nums">
                                        {row.totalScore} / {gradebook.totalMaxScore}
                                        <span className="text-slate-400 font-normal ml-2">{row.percent}%</span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            <p className="text-xs text-slate-400 mt-4">
                Прочерк — студент ещё не сдавал. Жёлтая клетка — балл поставлен вручную: автоматический
                пересчёт по решениям её не трогает, пока оценку не снимут.
            </p>

            {(editingItem || isCreatingItem) && (
                <GradeItemModal
                    courseId={courseId!}
                    item={editingItem}
                    onClose={() => {
                        setEditingItem(null);
                        setIsCreatingItem(false);
                    }}
                    onSaved={() => {
                        setEditingItem(null);
                        setIsCreatingItem(false);
                        fetchGradebook();
                    }}
                />
            )}

            {editingCell && (
                <GradeCellModal
                    item={editingCell.item}
                    userId={editingCell.userId}
                    studentName={editingCell.name}
                    current={gradebook.rows.find((r) => r.userId === editingCell.userId)?.scores[String(editingCell.item.id)]}
                    onClose={() => setEditingCell(null)}
                    onSaved={() => {
                        setEditingCell(null);
                        fetchGradebook();
                    }}
                />
            )}
        </div>
    );
};

const GradeItemModal = ({
    courseId,
    item,
    onClose,
    onSaved,
}: {
    courseId: string;
    item: GradeItemDto | null;
    onClose: () => void;
    onSaved: () => void;
}) => {
    const [title, setTitle] = useState(item?.title ?? '');
    const [maxScore, setMaxScore] = useState(item?.maxScore ?? 1);
    const [policy, setPolicy] = useState<GradePolicy>(item?.policy ?? 'BEST');
    const [isSaving, setIsSaving] = useState(false);

    const handleSave = async () => {
        setIsSaving(true);
        try {
            const payload = { title, maxScore, policy };
            if (item) {
                await apiClient.put(`/teacher/grade-items/${item.id}`, payload);
            } else {
                await apiClient.post(`/teacher/courses/${courseId}/grade-items`, payload);
            }
            onSaved();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось сохранить столбец');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <Modal title={item ? 'Настройки столбца' : 'Новый ручной столбец'} onClose={onClose}>
            <div className="space-y-4">
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Название</label>
                    <input
                        type="text"
                        value={title}
                        maxLength={200}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="Например: Экзамен"
                        className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Вес в курсе (баллы)</label>
                    <input
                        type="number"
                        min={1}
                        value={maxScore}
                        onChange={(e) => setMaxScore(Math.max(1, Number(e.target.value)))}
                        className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <p className="text-xs text-slate-400 mt-1">
                        Это вес именно в этом курсе: та же задача в другом курсе может стоить иначе.
                    </p>
                </div>
                {item?.kind !== 'MANUAL' && (
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Что идёт в зачёт</label>
                        <select
                            value={policy}
                            onChange={(e) => setPolicy(e.target.value as GradePolicy)}
                            className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                        >
                            {(Object.keys(POLICY_LABELS) as GradePolicy[]).map((value) => (
                                <option key={value} value={value}>{POLICY_LABELS[value]}</option>
                            ))}
                        </select>
                        <p className="text-xs text-slate-400 mt-1">
                            Смена правила или веса сразу пересчитывает уже выставленные баллы.
                        </p>
                    </div>
                )}
            </div>
            <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end gap-3">
                <button onClick={onClose} className="px-5 py-2.5 text-slate-500 hover:text-slate-800 transition-colors">
                    Отмена
                </button>
                <button
                    onClick={handleSave}
                    disabled={isSaving || !title.trim()}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-xl font-bold transition-colors disabled:opacity-50"
                >
                    <Save size={18} /> {isSaving ? 'Сохранение...' : 'Сохранить'}
                </button>
            </div>
        </Modal>
    );
};

const GradeCellModal = ({
    item,
    userId,
    studentName,
    current,
    onClose,
    onSaved,
}: {
    item: GradeItemDto;
    userId: number;
    studentName: string;
    current?: { score: number; manual: boolean; comment: string | null };
    onClose: () => void;
    onSaved: () => void;
}) => {
    const [score, setScore] = useState(current?.score ?? 0);
    const [comment, setComment] = useState(current?.comment ?? '');
    const [isSaving, setIsSaving] = useState(false);

    const handleSave = async () => {
        setIsSaving(true);
        try {
            await apiClient.put('/teacher/grades', { gradeItemId: item.id, userId, score, comment });
            onSaved();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось выставить балл');
        } finally {
            setIsSaving(false);
        }
    };

    const handleClear = async () => {
        setIsSaving(true);
        try {
            await apiClient.delete(`/teacher/grades/${item.id}/${userId}`);
            onSaved();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось снять оценку');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <Modal title={`${studentName} · ${item.title}`} onClose={onClose}>
            <div className="space-y-4">
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">
                        Балл (максимум {item.maxScore})
                    </label>
                    <input
                        type="number"
                        min={0}
                        max={item.maxScore}
                        value={score}
                        onChange={(e) => setScore(Number(e.target.value))}
                        className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Причина</label>
                    <textarea
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder="Почему балл выставлен вручную — студент увидит этот текст"
                        className="w-full h-24 p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 resize-y"
                    />
                </div>
                {item.kind === 'PROBLEM' && (
                    <p className="text-xs text-slate-500 bg-slate-50 border border-slate-200 rounded-xl p-3">
                        Ручной балл замораживает клетку: решения студента её больше не меняют, пока оценку
                        не снять.
                    </p>
                )}
            </div>
            <div className="mt-6 pt-4 border-t border-slate-100 flex justify-between gap-3">
                {current?.manual ? (
                    <button
                        onClick={handleClear}
                        disabled={isSaving}
                        className="px-5 py-2.5 text-red-600 hover:bg-red-50 rounded-xl transition-colors disabled:opacity-50"
                    >
                        Снять ручную оценку
                    </button>
                ) : (
                    <span />
                )}
                <div className="flex gap-3">
                    <button onClick={onClose} className="px-5 py-2.5 text-slate-500 hover:text-slate-800 transition-colors">
                        Отмена
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={isSaving || !comment.trim()}
                        className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-xl font-bold transition-colors disabled:opacity-50"
                    >
                        <Save size={18} /> {isSaving ? 'Сохранение...' : 'Выставить'}
                    </button>
                </div>
            </div>
        </Modal>
    );
};

const Modal = ({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) => (
    <div className="fixed inset-0 bg-slate-900/50 flex items-start justify-center p-4 z-50 overflow-y-auto">
        <div className="bg-white rounded-2xl shadow-xl max-w-lg w-full p-6 my-8">
            <div className="flex items-center justify-between mb-5">
                <h2 className="text-lg font-bold text-slate-800">{title}</h2>
                <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
                    <X size={20} />
                </button>
            </div>
            {children}
        </div>
    </div>
);
