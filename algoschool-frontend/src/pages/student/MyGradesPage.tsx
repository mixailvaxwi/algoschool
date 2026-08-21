import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import type { MyGradesDto } from '../../types/grade';
import { ArrowLeft, Award, MessageSquare } from 'lucide-react';

/** Свои оценки по курсу: только чтение, без возможности что-либо оспорить прямо здесь. */
export const MyGradesPage = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();

    const [data, setData] = useState<MyGradesDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        apiClient
            .get<MyGradesDto>(`/courses/${courseId}/my-grades`)
            .then((response) => setData(response.data))
            .catch((e: any) => setError(e.response?.data?.message || 'Не удалось загрузить оценки'))
            .finally(() => setIsLoading(false));
    }, [courseId]);

    if (isLoading) return <div className="text-slate-400 text-sm py-12 text-center">Загрузка оценок...</div>;
    if (error) return <div className="text-slate-600 py-12 text-center">{error}</div>;
    if (!data) return null;

    return (
        <div className="max-w-3xl mx-auto">
            <button
                onClick={() => navigate(`/courses/${courseId}`)}
                className="flex items-center gap-2 text-slate-500 mb-6 hover:text-slate-800 transition-colors"
            >
                <ArrowLeft size={18} /> К курсу
            </button>

            <div className="flex items-start justify-between gap-6 mb-8">
                <div>
                    <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                        <Award className="text-blue-600" size={22} /> Мои оценки
                    </h1>
                    <p className="text-slate-500 text-sm mt-1">{data.courseTitle}</p>
                </div>
                <div className="text-right shrink-0">
                    <div className="text-3xl font-bold text-slate-800 tabular-nums">
                        {data.totalScore}
                        <span className="text-slate-400 text-xl"> / {data.totalMaxScore}</span>
                    </div>
                    <div className="text-sm text-slate-500 tabular-nums">{data.percent}%</div>
                </div>
            </div>

            {data.grades.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    В курсе пока нет оцениваемых заданий.
                </div>
            ) : (
                <div className="border border-slate-200 rounded-xl divide-y divide-slate-100">
                    {data.grades.map((grade) => (
                        <div key={grade.itemId} className="flex items-start justify-between gap-4 p-4">
                            <div className="min-w-0">
                                {grade.lessonId ? (
                                    <Link
                                        to={`/courses/${courseId}/lessons/${grade.lessonId}`}
                                        className="font-medium text-slate-800 hover:text-blue-600 transition-colors"
                                    >
                                        {grade.title}
                                    </Link>
                                ) : (
                                    <span className="font-medium text-slate-800">{grade.title}</span>
                                )}
                                {grade.comment && (
                                    <p className="text-sm text-slate-500 mt-1 flex items-start gap-1.5">
                                        <MessageSquare size={14} className="shrink-0 mt-0.5" />
                                        {grade.comment}
                                    </p>
                                )}
                            </div>
                            <div className="shrink-0 tabular-nums text-right">
                                {grade.score === null ? (
                                    <span className="text-slate-300">— / {grade.maxScore}</span>
                                ) : (
                                    <span className="font-semibold text-slate-800">
                                        {grade.score}
                                        <span className="text-slate-400 font-normal"> / {grade.maxScore}</span>
                                    </span>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <p className="text-xs text-slate-400 mt-4">
                Прочерк означает, что задание ещё не сдано. Если по задаче было несколько попыток, в зачёт
                идёт та, которую выбрал преподаватель, — обычно лучшая.
            </p>
        </div>
    );
};
