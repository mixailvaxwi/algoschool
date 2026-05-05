import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { CheckCircle, XCircle, User, Clock, ArrowLeft, Inbox } from 'lucide-react';

interface Application {
    id: number;
    courseId: number;
    courseTitle: string;
    studentId: number;
    studentName: string;
    motivationMessage: string;
    status: string;
    createdAt: string;
}

export const CourseApplicationsPage = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const [applications, setApplications] = useState<Application[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [processingId, setProcessingId] = useState<number | null>(null);

    // Загружаем список заявок
    useEffect(() => {
        const fetchApplications = async () => {
            try {
                // Тот самый эндпоинт, который мы написали на бэкенде
                const response = await apiClient.get(`/api/teacher/courses/${courseId}/applications`);
                setApplications(response.data);
            } catch (err) {
                setError('Не удалось загрузить список заявок. Проверьте права доступа.');
            } finally {
                setIsLoading(false);
            }
        };

        if (courseId) fetchApplications();
    }, [courseId]);

    // Обработчик принятия/отклонения
    const handleAction = async (appId: number, isApproved: boolean) => {
        setProcessingId(appId);
        try {
            const action = isApproved ? 'approve' : 'reject';
            await apiClient.post(`/api/teacher/applications/${appId}/${action}`);

            // Если всё успешно - убираем заявку из списка (чтобы препод сразу видел результат)
            setApplications(prev => prev.filter(app => app.id !== appId));
        } catch (err) {
            alert('Произошла ошибка при обработке заявки.');
        } finally {
            setProcessingId(null);
        }
    };

    if (isLoading) {
        return <div className="text-center py-20 text-slate-500">Загрузка заявок...</div>;
    }

    if (error) {
        return <div className="text-center py-20 text-red-500">{error}</div>;
    }

    const courseTitle = applications.length > 0 ? applications[0].courseTitle : 'курса';

    return (
        <div className="max-w-5xl mx-auto p-4 py-8">
            {/* Навигация и заголовок */}
            <div className="mb-8">
                <Link
                    to="/teacher/courses"
                    className="inline-flex items-center gap-2 text-slate-500 hover:text-blue-600 transition-colors mb-4"
                >
                    <ArrowLeft size={16} /> Назад к моим курсам
                </Link>
                <h1 className="text-3xl font-bold text-slate-800">
                    Заявки на курс <span className="text-blue-600">«{courseTitle}»</span>
                </h1>
                <p className="text-slate-600 mt-2">
                    Здесь отображаются студенты, которые ожидают вашего одобрения для начала обучения.
                </p>
            </div>

            {/* Состояние, когда заявок нет */}
            {applications.length === 0 ? (
                <div className="bg-white rounded-2xl border border-slate-200 p-12 flex flex-col items-center justify-center text-center">
                    <div className="bg-slate-50 p-4 rounded-full mb-4">
                        <Inbox size={48} className="text-slate-400" />
                    </div>
                    <h3 className="text-xl font-bold text-slate-700 mb-2">Новых заявок пока нет</h3>
                    <p className="text-slate-500 max-w-md">
                        Все желающие уже обработаны или студенты пока не подавали мотивационные письма на этот курс.
                    </p>
                </div>
            ) : (
                /* Список (таблица) заявок */
                <div className="space-y-4">
                    {applications.map((app) => (
                        <div
                            key={app.id}
                            className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-6 md:items-center transition-all hover:border-blue-200"
                        >
                            {/* Инфо о студенте */}
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-3">
                                    <div className="bg-blue-100 p-2 rounded-full text-blue-600">
                                        <User size={20} />
                                    </div>
                                    <div>
                                        <h3 className="font-bold text-slate-800 text-lg">{app.studentName}</h3>
                                        <div className="flex items-center gap-1 text-xs text-slate-500">
                                            <Clock size={12} />
                                            {new Date(app.createdAt).toLocaleDateString('ru-RU', {
                                                day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit'
                                            })}
                                        </div>
                                    </div>
                                </div>

                                {/* Мотивационное письмо */}
                                <div className="bg-slate-50 p-4 rounded-xl text-slate-700 text-sm border border-slate-100 italic relative">
                                    <span className="absolute -top-2 left-4 bg-slate-50 px-1 text-xs text-slate-400 font-medium">Сообщение</span>
                                    {app.motivationMessage}
                                </div>
                            </div>

                            {/* Кнопки действий */}
                            <div className="flex flex-row md:flex-col gap-3 shrink-0">
                                <button
                                    onClick={() => handleAction(app.id, true)}
                                    disabled={processingId === app.id}
                                    className="flex-1 md:flex-none flex items-center justify-center gap-2 px-5 py-3 bg-emerald-500 hover:bg-emerald-600 disabled:bg-emerald-300 text-white font-medium rounded-xl transition-colors"
                                    title="Одобрить и зачислить"
                                >
                                    <CheckCircle size={18} />
                                    Одобрить
                                </button>

                                <button
                                    onClick={() => handleAction(app.id, false)}
                                    disabled={processingId === app.id}
                                    className="flex-1 md:flex-none flex items-center justify-center gap-2 px-5 py-3 bg-white border-2 border-red-100 text-red-500 hover:bg-red-50 disabled:opacity-50 font-medium rounded-xl transition-colors"
                                    title="Отклонить заявку"
                                >
                                    <XCircle size={18} />
                                    Отказать
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};