import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { useAuthStore } from '../store/authStore';
import { Unlock, BookOpen, Lock, LogIn } from 'lucide-react';

// ИЗМЕНЕНО: Интерфейс теперь совпадает с CourseCatalogResponse
interface Course {
    id: number;
    title: string;
    description: string;
    accessType: 'OPEN' | 'CLOSED';
    isEnrolled: boolean;
}

export const CatalogPage = () => {
    const [courses, setCourses] = useState<Course[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [processingId, setProcessingId] = useState<number | null>(null);

    const navigate = useNavigate();
    // ИЗМЕНЕНО: Убрали user и updateBalance, они здесь больше не нужны
    const { isAuthenticated } = useAuthStore();

    useEffect(() => {
        const fetchCourses = async () => {
            try {
                const response = await apiClient.get('/courses');
                setCourses(response.data);
            } catch (err) {
                setError('Не удалось загрузить каталог курсов');
            } finally {
                setIsLoading(false);
            }
        };

        fetchCourses();
    }, []);

    // ИЗМЕНЕНО: Новая логика записи на курс
    const handleEnroll = async (courseId: number, accessType: string) => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        if (accessType === 'CLOSED') {
            alert('На этот курс можно попасть только по заявке. (Функционал заявок в разработке)');
            return;
        }

        setProcessingId(courseId);
        try {
            // ИЗМЕНЕНО: Новый эндпоинт для записи
            await apiClient.post(`/courses/${courseId}/enroll`);

            // Обновляем статус конкретного курса в стейте
            setCourses(prev => prev.map(c =>
                c.id === courseId ? { ...c, isEnrolled: true } : c
            ));

        } catch (err: any) {
            console.error("Ошибка от сервера:", err.response);
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
                {error && <div className="text-red-500 mt-2">{error}</div>}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {courses.map((course) => (
                    <div key={course.id} className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden flex flex-col hover:shadow-md transition-shadow">
                        <div className="h-32 bg-gradient-to-r from-blue-500 to-indigo-600 flex items-center justify-center">
                            <BookOpen size={48} className="text-white opacity-20" />
                        </div>

                        <div className="p-6 flex flex-col flex-grow">
                            <h2 className="text-xl font-bold text-slate-800 mb-2">{course.title}</h2>
                            <p className="text-slate-600 text-sm mb-6 flex-grow">{course.description}</p>

                            <div className="mt-auto pt-4 border-t border-slate-100">
                                {course.isEnrolled ? (
                                    <button
                                        onClick={() => navigate(`/courses/${course.id}`)}
                                        className="w-full flex items-center justify-center gap-2 py-2.5 bg-emerald-50 text-emerald-600 font-medium rounded-lg hover:bg-emerald-100 transition-colors"
                                    >
                                        <Unlock size={18} />
                                        Перейти к урокам
                                    </button>
                                ) : (
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-1.5 font-medium text-sm">
                                            {course.accessType === 'OPEN' ? (
                                                <span className="text-blue-600 bg-blue-50 px-2 py-1 rounded-md">Открытый доступ</span>
                                            ) : (
                                                <span className="text-slate-500 bg-slate-100 px-2 py-1 rounded-md flex items-center gap-1">
                                                    <Lock size={14} /> По заявкам
                                                </span>
                                            )}
                                        </div>
                                        <button
                                            onClick={() => handleEnroll(course.id, course.accessType)}
                                            disabled={processingId === course.id}
                                            className={`flex items-center gap-2 py-2 px-4 font-medium rounded-lg transition-colors disabled:opacity-70 ${
                                                course.accessType === 'OPEN'
                                                    ? 'bg-blue-600 text-white hover:bg-blue-700'
                                                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                            }`}
                                        >
                                            {processingId === course.id
                                                ? 'Обработка...'
                                                : (course.accessType === 'OPEN' ? (
                                                    <>
                                                        <LogIn size={18} /> Записаться
                                                    </>
                                                ) : 'Подать заявку')
                                            }
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};