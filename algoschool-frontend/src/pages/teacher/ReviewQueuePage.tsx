import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { ArrowLeft, PenLine, Send, ClipboardCheck, BookOpen } from 'lucide-react';

interface ReviewItem {
    submissionId: number;
    studentId: number;
    studentName: string;
    courseId: number;
    courseTitle: string;
    lessonId: number;
    lessonTitle: string;
    stepId: number;
    problemTitle: string;
    description: string;
    /** Критерии автора задачи — подсказка проверяющему, студенту не видна. */
    reviewGuidelines: string | null;
    answer: string;
    maxScore: number;
    submittedAt: string;
}

/** Очередь ручной проверки развёрнутых ответов (UC-T-40). */
export const ReviewQueuePage = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();

    const [queue, setQueue] = useState<ReviewItem[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchQueue = useCallback(async () => {
        setIsLoading(true);
        try {
            const response = await apiClient.get<ReviewItem[]>('/teacher/review-queue', {
                params: { courseId },
            });
            setQueue(response.data);
        } catch (error) {
            console.error('Ошибка загрузки очереди проверки', error);
        } finally {
            setIsLoading(false);
        }
    }, [courseId]);

    useEffect(() => {
        fetchQueue();
    }, [fetchQueue]);

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
                        <ClipboardCheck className="text-blue-600" size={22} /> Очередь проверки
                    </h1>
                    <p className="text-slate-500 text-sm">
                        {queue.length > 0 ? queue[0].courseTitle : 'Развёрнутые ответы студентов'}
                    </p>
                </div>
                {queue.length > 0 && (
                    <span className="ml-auto bg-blue-50 text-blue-700 px-3 py-1.5 rounded-lg font-medium tabular-nums">
                        {queue.length} на проверке
                    </span>
                )}
            </div>

            {isLoading ? (
                <div className="text-slate-400 text-sm py-8 text-center">Загрузка...</div>
            ) : queue.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    Непроверенных ответов нет.
                </div>
            ) : (
                <div className="space-y-4">
                    {queue.map((item) => (
                        <ReviewCard key={item.submissionId} item={item} onReviewed={fetchQueue} />
                    ))}
                </div>
            )}
        </div>
    );
};

const ReviewCard = ({ item, onReviewed }: { item: ReviewItem; onReviewed: () => void }) => {
    const [score, setScore] = useState(item.maxScore);
    const [comment, setComment] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    const handleSubmit = async () => {
        setIsSaving(true);
        try {
            await apiClient.post(`/teacher/submissions/${item.submissionId}/review`, { score, comment });
            onReviewed();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось сохранить проверку');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="border border-slate-200 rounded-xl overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 bg-slate-50 border-b border-slate-200">
                <div className="min-w-0">
                    <span className="font-semibold text-slate-800">{item.studentName}</span>
                    <span className="text-slate-400 mx-2">·</span>
                    <span className="text-slate-600">{item.problemTitle}</span>
                </div>
                <div className="flex items-center gap-3 text-xs text-slate-500 shrink-0">
                    <span className="flex items-center gap-1">
                        <BookOpen size={13} /> {item.lessonTitle}
                    </span>
                    <span>{new Date(item.submittedAt).toLocaleString('ru-RU')}</span>
                </div>
            </div>

            <div className="p-4 space-y-4">
                <details className="text-sm">
                    <summary className="cursor-pointer text-slate-500 hover:text-slate-800 transition-colors">
                        Условие задачи
                    </summary>
                    <p className="mt-2 text-slate-600 whitespace-pre-wrap">{item.description}</p>
                </details>

                {item.reviewGuidelines && (
                    <div className="text-sm bg-blue-50 border border-blue-100 rounded-xl p-3">
                        <p className="font-semibold text-blue-900 mb-1">Критерии проверки</p>
                        <p className="text-blue-800 whitespace-pre-wrap">{item.reviewGuidelines}</p>
                    </div>
                )}

                <div>
                    <p className="text-sm font-semibold text-slate-700 mb-2 flex items-center gap-2">
                        <PenLine size={15} /> Ответ студента
                    </p>
                    <div className="bg-white border border-slate-200 rounded-xl p-4 whitespace-pre-wrap text-slate-800 leading-relaxed max-h-72 overflow-y-auto">
                        {item.answer}
                    </div>
                </div>

                <div className="flex flex-col sm:flex-row gap-3 sm:items-end">
                    <div className="sm:w-40">
                        <label className="block text-sm font-semibold text-slate-700 mb-2">
                            Балл (из {item.maxScore})
                        </label>
                        <input
                            type="number"
                            min={0}
                            max={item.maxScore}
                            value={score}
                            onChange={(e) => setScore(Number(e.target.value))}
                            className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 tabular-nums"
                        />
                    </div>
                    <div className="flex-1">
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Комментарий студенту</label>
                        <input
                            type="text"
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="За что этот балл — студент увидит текст в истории попыток"
                            className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <button
                        onClick={handleSubmit}
                        disabled={isSaving || !comment.trim()}
                        className="flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-xl font-bold transition-colors disabled:opacity-50 shrink-0"
                    >
                        <Send size={18} /> {isSaving ? 'Сохранение...' : 'Проверено'}
                    </button>
                </div>
            </div>
        </div>
    );
};
