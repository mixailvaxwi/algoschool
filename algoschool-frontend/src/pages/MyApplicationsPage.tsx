// @ts-ignore
import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/axios';
import { Clock, CheckCircle, XCircle, BookOpen, MessageSquare, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface Application {
    id: number;
    courseId: number;
    courseTitle: string;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    motivationMessage: string;
    createdAt: string;
}

export const MyApplicationsPage = () => {
    const [applications, setApplications] = useState<Application[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchApplications = async () => {
            try {
                const response = await apiClient.get('/applications/my');
                setApplications(response.data);
            } catch (error) {
                console.error("Ошибка при загрузке заявок", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchApplications();
    }, []);

    const getStatusStyles = (status: string) => {
        switch (status) {
            case 'PENDING':
                return {
                    icon: <Clock size={16} />,
                    text: 'На рассмотрении',
                    className: 'bg-yellow-50 text-yellow-700 border-yellow-200'
                };
            case 'APPROVED':
                return {
                    icon: <CheckCircle size={16} />,
                    text: 'Одобрена',
                    className: 'bg-emerald-50 text-emerald-700 border-emerald-200'
                };
            case 'REJECTED':
                return {
                    icon: <XCircle size={16} />,
                    text: 'Отклонена',
                    className: 'bg-red-50 text-red-700 border-red-200'
                };
            default:
                return { icon: null, text: status, className: '' };
        }
    };

    if (isLoading) return <div className="text-center py-20 text-slate-500">Загрузка ваших заявок...</div>;

    return (
        <div className="max-w-4xl mx-auto py-10 px-4">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-slate-500 mb-6 hover:text-slate-800 transition-colors"
            >
                <ArrowLeft size={18} /> Назад
            </button>

            <h1 className="text-3xl font-bold text-slate-800 mb-8">Мои заявки на курсы</h1>

            {applications.length === 0 ? (
                <div className="bg-white p-12 rounded-2xl border border-slate-200 text-center shadow-sm">
                    <div className="bg-slate-50 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                        <BookOpen className="text-slate-400" size={32} />
                    </div>
                    <h2 className="text-xl font-semibold text-slate-700 mb-2">Заявок пока нет</h2>
                    <p className="text-slate-500 mb-6">Вы еще не подавали заявки на закрытые курсы.</p>
                    <button
                        onClick={() => navigate('/catalog')}
                        className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Перейти в каталог
                    </button>
                </div>
            ) : (
                <div className="grid gap-4">
                    {applications.map((app) => {
                        const styles = getStatusStyles(app.status);
                        return (
                            <div key={app.id} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                                    <div className="flex-1">
                                        <h3 className="text-xl font-bold text-slate-800 mb-2">{app.courseTitle}</h3>
                                        <div className="flex items-center gap-4 text-sm text-slate-500 mb-4">
                                            <span className="flex items-center gap-1.5">
                                                <Clock size={14} />
                                                {new Date(app.createdAt).toLocaleDateString('ru-RU')}
                                            </span>
                                            {app.motivationMessage && (
                                                <span className="flex items-center gap-1.5">
                                                    <MessageSquare size={14} /> Есть сообщение
                                                </span>
                                            )}
                                        </div>

                                        {app.motivationMessage && (
                                            <div className="bg-slate-50 p-3 rounded-lg text-sm text-slate-600 italic border-l-4 border-slate-200">
                                                "{app.motivationMessage}"
                                            </div>
                                        )}
                                    </div>

                                    <div className="flex flex-col items-start md:items-end gap-3">
                                        <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold border ${styles.className}`}>
                                            {styles.icon}
                                            {styles.text}
                                        </div>

                                        {app.status === 'APPROVED' && (
                                            <button
                                                onClick={() => navigate(`/courses/${app.courseId}`)}
                                                className="text-blue-600 hover:text-blue-800 text-sm font-bold transition-colors"
                                            >
                                                Перейти к курсу →
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};