import React, { useState, useEffect } from 'react';
import { apiClient } from '../../api/axios';
import { useParams } from 'react-router-dom';
import { CheckCircle, XCircle, Clock, Terminal, ChevronDown, ChevronUp, AlertTriangle } from 'lucide-react';

interface HistoryRecord {
    id: number;
    payload: string;
    status: string;
    createdAt: string;
}

interface Props {
    stepId: number;
    refreshKey: number; // Триггер для перезагрузки при новой попытке
}

export const SubmissionHistory: React.FC<Props> = ({ stepId, refreshKey }) => {
    const { courseId, lessonId } = useParams();
    const [history, setHistory] = useState<HistoryRecord[]>([]);
    const [showAll, setShowAll] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchHistory = async () => {
            try {
                const response = await apiClient.get(
                    `/courses/${courseId}/lessons/${lessonId}/steps/${stepId}/submit/history`
                );
                setHistory(response.data);
            } catch (err) {
                console.error("Не удалось загрузить историю попыток", err);
            } finally {
                setIsLoading(false);
            }
        };
        fetchHistory();
    }, [courseId, lessonId, stepId, refreshKey]);

    if (isLoading) return <div className="text-slate-400 text-sm mt-4">Загрузка истории...</div>;
    if (history.length === 0) return null;

    const visibleHistory = showAll ? history : history.slice(0, 3);

    return (
        <div className="mt-8 pt-6 border-t border-slate-200">
            <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                <Clock size={20} className="text-slate-400" />
                История попыток
                <span className="bg-slate-100 text-slate-500 text-xs px-2 py-0.5 rounded-full">{history.length}</span>
            </h3>

            <div className="space-y-3">
                {visibleHistory.map((record, index) => {
                    const isCorrect = record.status === 'CORRECT';
                    const isPending = record.status === 'PENDING';
                    // Сбой проверяющей системы — не вердикт по решению.
                    // Красный крестик здесь читался бы как «ответ неверный».
                    const isFailed = record.status === 'SUBMISSION_FAILED';

                    return (
                        <div key={record.id} className="bg-slate-50 border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                            <div className="flex items-center gap-3">
                                <div className="shrink-0 mt-0.5 sm:mt-0">
                                    {isCorrect ? <CheckCircle className="text-emerald-500" size={20} />
                                        : isPending ? <Clock className="text-amber-500" size={20} />
                                            : isFailed ? <AlertTriangle className="text-orange-500" size={20} />
                                                : <XCircle className="text-red-500" size={20} />}
                                </div>
                                <div>
                                    <div className="font-medium text-slate-800 text-sm">
                                        Попытка #{history.length - index}
                                        {isFailed && <span className="ml-2 font-normal text-orange-600">не удалось отправить на проверку</span>}
                                    </div>
                                    <div className="text-xs text-slate-500">
                                        {new Date(record.createdAt).toLocaleString('ru-RU')}
                                    </div>
                                </div>
                            </div>

                            <div className="bg-white px-3 py-1.5 rounded-lg border border-slate-100 shadow-sm text-sm font-mono text-slate-600 truncate max-w-xs sm:max-w-sm flex items-center gap-2">
                                <Terminal size={14} className="text-slate-400" />
                                <span className="truncate">{record.payload}</span>
                            </div>
                        </div>
                    );
                })}
            </div>

            {history.length > 3 && (
                <button
                    onClick={() => setShowAll(!showAll)}
                    className="mt-4 w-full py-2 flex items-center justify-center gap-2 text-sm font-medium text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-lg transition-colors"
                >
                    {showAll ? (
                        <><ChevronUp size={16} /> Скрыть старые попытки</>
                    ) : (
                        <><ChevronDown size={16} /> Показать все ({history.length})</>
                    )}
                </button>
            )}
        </div>
    );
};