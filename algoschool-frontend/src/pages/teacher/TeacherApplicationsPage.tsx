import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { Check, X, ArrowLeft, Clock } from 'lucide-react';

interface Application {
    id: number;
    studentName: string;
    motivationMessage: string;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export const TeacherApplicationsPage = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();
    const [applications, setApplications] = useState<Application[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetchApplications();
    }, [courseId]);

    const fetchApplications = async () => {
        try {
            const response = await apiClient.get(`/teacher/courses/${courseId}/applications`);
            setApplications(response.data);
        } catch (error) {
            console.error("Ошибка загрузки заявок", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleUpdateStatus = async (appId: number, newStatus: 'APPROVED' | 'REJECTED') => {
        try {
            await apiClient.put(`/teacher/courses/${courseId}/applications/${appId}/status`, {
                status: newStatus
            });

            // Обновляем список локально, чтобы не делать лишний запрос
            setApplications(prev => prev.map(app =>
                app.id === appId ? { ...app, status: newStatus } : app
            ));
        } catch (error) {
            alert('Ошибка при обновлении статуса');
        }
    };

    if (isLoading) return <div className="p-8 text-center text-slate-500">Загрузка заявок...</div>;

    return (
        <div className="max-w-4xl mx-auto py-8">
            <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-slate-500 mb-6 hover:text-slate-800">
                <ArrowLeft size={18} /> К управлению курсом
            </button>

            <h1 className="text-2xl font-bold text-slate-800 mb-6">Заявки на курс</h1>

            {applications.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    Пока нет ни одной заявки.
                </div>
            ) : (
                <div className="space-y-4">
                    {applications.map(app => (
                        <div key={app.id} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-2">
                                    <h3 className="font-semibold text-lg">{app.studentName}</h3>
                                    {app.status === 'PENDING' && <span className="bg-yellow-100 text-yellow-700 text-xs px-2 py-1 rounded-md flex items-center gap-1"><Clock size={12}/> Рассматривается</span>}
                                    {app.status === 'APPROVED' && <span className="bg-emerald-100 text-emerald-700 text-xs px-2 py-1 rounded-md">Одобрено</span>}
                                    {app.status === 'REJECTED' && <span className="bg-red-100 text-red-700 text-xs px-2 py-1 rounded-md">Отклонено</span>}
                                </div>
                                <p className="text-slate-600 italic border-l-4 border-slate-200 pl-3">
                                    "{app.motivationMessage || 'Без сопроводительного письма'}"
                                </p>
                            </div>

                            {/* Кнопки действий показываем только если заявка PENDING */}
                            {app.status === 'PENDING' && (
                                <div className="flex gap-2 ml-6">
                                    <button
                                        onClick={() => handleUpdateStatus(app.id, 'APPROVED')}
                                        className="p-2 bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white rounded-lg transition-colors flex items-center gap-2 font-medium"
                                    >
                                        <Check size={18} /> Одобрить
                                    </button>
                                    <button
                                        onClick={() => handleUpdateStatus(app.id, 'REJECTED')}
                                        className="p-2 bg-red-50 text-red-600 hover:bg-red-500 hover:text-white rounded-lg transition-colors flex items-center gap-2 font-medium"
                                    >
                                        <X size={18} /> Отклонить
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};