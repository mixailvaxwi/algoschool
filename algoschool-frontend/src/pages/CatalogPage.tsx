import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { useAuthStore } from '../store/authStore';
import {BookOpen, User} from 'lucide-react';

interface Course {
    id: number;
    title: string;
    description: string;
    accessType: 'OPEN' | 'CLOSED';
    authorName: string;
    lessonsCount: number;
    isEnrolled: boolean;
    applicationStatus: string;
}

export const CatalogPage = () => {
    const [courses, setCourses] = useState<Course[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [processingId, setProcessingId] = useState<number | null>(null);

    const navigate = useNavigate();
    const { isAuthenticated } = useAuthStore();

    useEffect(() => {
        const fetchCourses = async () => {
            try {
                const response = await apiClient.get('/courses');

                // Безопасное сохранение в стейт
                if (Array.isArray(response.data)) {
                    setCourses(response.data);
                } else if (response.data && Array.isArray(response.data.content)) {
                    // Если вдруг Spring вернул пагинацию или обертку content
                    setCourses(response.data.content);
                } else if (response.data && Array.isArray(response.data.data)) {
                    // Если обертка называется data
                    setCourses(response.data.data);
                } else {
                    // Сервер вернул что-то непонятное (например HTML)
                    setCourses([]);
                    setError('Неверный формат данных от сервера. Посмотрите консоль!');
                }

            } catch (err) {
                setError('Не удалось загрузить каталог курсов');
            } finally {
                setIsLoading(false);
            }
        };

        fetchCourses();
    }, []);

    const handleEnroll = async (courseId: number, accessType: string) => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        let payload = {};

        if (accessType === 'CLOSED') {
            const message = window.prompt("Напишите пару слов, почему вы хотите на этот курс:");
            if (!message) return; // Пользователь нажал "Отмена"
            payload = { motivationMessage: message };
        }

        setProcessingId(courseId);

        try {
            const response = await apiClient.post(`/courses/${courseId}/enroll`, payload);
            alert(response.data.message || 'Вы успешно записаны!');

            // Обновляем состояние кнопки
            setCourses(prev => prev.map(c =>
                c.id === courseId ? { ...c, isEnrolled: true } : c
            ));
        } catch (err: any) {
            alert(err.response?.data?.message || 'Произошла ошибка при записи на курс.');
        } finally {
            setProcessingId(null);
        }
    };

    if (isLoading) {
        return <div className="text-center py-20 text-slate-500">Загрузка каталога...</div>;
    }

    return (
        <div className="max-w-6xl mx-auto">
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-slate-800">Каталог курсов</h1>
                <p className="text-slate-500 mt-2">Выбирайте открытые курсы и начинайте обучение прямо сейчас</p>
                {error && <div className="text-red-500 mt-2 font-medium bg-red-50 p-3 rounded-lg border border-red-100">{error}</div>}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {courses.map(course => (
                    <div
                        key={course.id}
                        className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col hover:shadow-md transition-shadow"
                    >
                        {/* КЛИКАБЕЛЬНАЯ ВЕРХНЯЯ ЧАСТЬ (Тело карточки) */}
                        <div
                            className="p-6 flex-1 cursor-pointer group"
                            onClick={() => navigate(`/courses/${course.id}`)}
                        >
                            <div className="flex justify-between items-start mb-4">
                                <h2 className="text-xl font-bold text-slate-800 group-hover:text-blue-600 transition-colors line-clamp-2">
                                    {course.title}
                                </h2>
                                {course.accessType === 'OPEN' ? (
                                    <span className="bg-emerald-100 text-emerald-700 text-xs font-bold px-2 py-1 rounded-md shrink-0">Открытый</span>
                                ) : (
                                    <span className="bg-purple-100 text-purple-700 text-xs font-bold px-2 py-1 rounded-md shrink-0">По заявкам</span>
                                )}
                            </div>

                            <p className="text-slate-600 mb-6 line-clamp-3 text-sm">
                                {course.description || 'Описание не указано.'}
                            </p>

                            <div className="flex items-center gap-4 text-sm text-slate-500 mt-auto">
                                <span className="flex items-center gap-1.5"><User size={16} /> {course.authorName}</span>
                                <span className="flex items-center gap-1.5"><BookOpen size={16} /> {course.lessonsCount} уроков</span>
                            </div>
                        </div>

                        {/* НЕКЛИКАБЕЛЬНЫЙ ПОДВАЛ С КНОПКАМИ */}
                        <div className="p-6 bg-slate-50 border-t border-slate-100 mt-auto">
                            {course.isEnrolled ? (
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation(); // Останавливаем всплытие клика
                                        navigate(`/courses/${course.id}`); // Или сразу в плеер: navigate(`/player/${course.id}`)
                                    }}
                                    className="w-full bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-2.5 rounded-lg font-medium transition-colors"
                                >
                                    Перейти к урокам
                                </button>
                            ) : course.applicationStatus === 'PENDING' ? (
                                <button disabled className="w-full bg-yellow-500 text-white px-4 py-2.5 rounded-lg font-medium opacity-80 cursor-not-allowed">
                                    ⏳ Заявка на рассмотрении
                                </button>
                            ) : course.applicationStatus === 'REJECTED' ? (
                                <button disabled className="w-full bg-red-500 text-white px-4 py-2.5 rounded-lg font-medium opacity-80 cursor-not-allowed">
                                    ❌ Заявка отклонена
                                </button>
                            ) : (
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation(); // Останавливаем всплытие
                                        handleEnroll(course.id, course.accessType);
                                    }}
                                    disabled={processingId === course.id}
                                    className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white px-4 py-2.5 rounded-lg font-medium transition-colors"
                                >
                                    {processingId === course.id ? 'Обработка...' : (course.accessType === 'CLOSED' ? 'Подать заявку' : 'Записаться')}
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};