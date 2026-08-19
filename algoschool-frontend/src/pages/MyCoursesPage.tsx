// @ts-ignore
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { BookOpen, User, ArrowRight } from 'lucide-react';

interface Course {
    id: number;
    title: string;
    description: string;
    authorName: string;
    lessonsCount: number;
}

export const MyCoursesPage = () => {
    const [courses, setCourses] = useState<Course[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchMyCourses = async () => {
            try {
                const response = await apiClient.get('/courses/enrolled');
                setCourses(response.data);
            } catch (error) {
                console.error("Ошибка при загрузке ваших курсов", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchMyCourses();
    }, []);

    if (isLoading) return <div className="text-center py-20 text-slate-500">Загрузка ваших курсов...</div>;

    return (
        <div className="max-w-7xl mx-auto py-10 px-4">
            <div className="flex items-center gap-3 mb-8">
                <div className="bg-blue-600 text-white p-2 rounded-lg shadow-lg">
                    <BookOpen size={24} />
                </div>
                <h1 className="text-3xl font-bold text-slate-800">Мое обучение</h1>
            </div>

            {courses.length === 0 ? (
                <div className="bg-white p-12 rounded-2xl border border-dashed border-slate-300 text-center shadow-sm">
                    <h2 className="text-xl font-semibold text-slate-700 mb-2">У вас пока нет активных курсов</h2>
                    <p className="text-slate-500 mb-6">Запишитесь на открытые курсы или подайте заявку в каталоге.</p>
                    <button
                        onClick={() => navigate('/catalog')}
                        className="bg-blue-600 text-white px-8 py-3 rounded-xl hover:bg-blue-700 transition-colors font-medium"
                    >
                        Перейти в каталог
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {courses.map(course => (
                        <div key={course.id} className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col hover:shadow-md transition-shadow">

                            {/* КЛИКАБЕЛЬНАЯ ВЕРХНЯЯ ЧАСТЬ */}
                            <div
                                className="p-6 flex-1 cursor-pointer group"
                                onClick={() => navigate(`/courses/${course.id}`)}
                            >
                                <h2 className="text-xl font-bold text-slate-800 mb-3 group-hover:text-blue-600 transition-colors">
                                    {course.title}
                                </h2>
                                <p className="text-slate-600 text-sm line-clamp-2 mb-6">
                                    {course.description || 'Описание не указано.'}
                                </p>
                                <div className="flex items-center gap-4 text-xs text-slate-400">
                                    <span className="flex items-center gap-1"><User size={14} /> {course.authorName}</span>
                                    <span className="flex items-center gap-1"><BookOpen size={14} /> {course.lessonsCount} уроков</span>
                                </div>
                            </div>

                            {/* ПОДВАЛ С КНОПКОЙ */}
                            <div className="p-6 pt-0 mt-auto">
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation(); // Чтобы клик не срабатывал дважды
                                        navigate(`/courses/${course.id}`);
                                    }}
                                    className="w-full flex items-center justify-center gap-2 bg-slate-900 hover:bg-blue-600 text-white px-4 py-3 rounded-xl font-medium transition-all"
                                >
                                    Продолжить обучение <ArrowRight size={18} />
                                </button>
                            </div>

                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};