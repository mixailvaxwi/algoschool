import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { ChevronLeft, ChevronRight, BookOpen } from 'lucide-react';
import { StepRenderer } from '../components/player/StepRenderer'; // Подключаем наш новый рендерер!
import type { AnyStep } from '../components/player/playerTypes';

interface Lesson {
    id: number;
    title: string;
    steps: AnyStep[];
}

export const LessonPage = () => {
    // Теперь нам нужны оба параметра для корректной работы API
    const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
    const navigate = useNavigate();

    const [lesson, setLesson] = useState<Lesson | null>(null);
    const [currentStepIndex, setCurrentStepIndex] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchLesson = async () => {
            try {
                // Если ваш бекенд требует courseId в пути, обновите урл: `/courses/${courseId}/lessons/${lessonId}`
                const response = await apiClient.get(`/courses/${courseId}/lessons/${lessonId}`);
                setLesson(response.data);
            } catch (err) {
                setError('Не удалось загрузить урок. Возможно, у вас нет доступа.');
            } finally {
                setIsLoading(false);
            }
        };
        fetchLesson();
    }, [lessonId]);

    if (isLoading) return <div className="text-center py-20 text-slate-500">Загрузка урока...</div>;
    if (error || !lesson) return <div className="text-center py-20 text-red-500">{error || 'Урок не найден'}</div>;

    const currentStep = lesson.steps[currentStepIndex];

    return (
        <div className="max-w-4xl mx-auto">
            <div className="mb-8 flex items-center justify-between">
                <div>
                    <button
                        onClick={() => navigate(-1)}
                        className="text-slate-500 hover:text-slate-800 flex items-center gap-1 mb-2 text-sm transition-colors"
                    >
                        <ChevronLeft size={16} /> Назад к курсу
                    </button>
                    <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                        <BookOpen className="text-blue-600" size={24} />
                        {lesson.title}
                    </h1>
                </div>
                <div className="text-sm font-medium text-slate-500 bg-slate-100 px-3 py-1 rounded-full">
                    Шаг {currentStepIndex + 1} из {lesson.steps.length}
                </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <div className="w-full bg-slate-100 h-1.5">
                    <div
                        className="bg-blue-600 h-full transition-all duration-300"
                        style={{ width: `${((currentStepIndex + 1) / lesson.steps.length) * 100}%` }}
                    />
                </div>

                <div className="p-8">
                    {/* ИСПОЛЬЗУЕМ НАШ НОВЫЙ ВНЕШНИЙ КОМПОНЕНТ */}
                    {currentStep && <StepRenderer step={currentStep} />}
                </div>

                <div className="p-6 bg-slate-50 border-t border-slate-200 flex justify-between items-center">
                    <button
                        onClick={() => setCurrentStepIndex(prev => Math.max(0, prev - 1))}
                        disabled={currentStepIndex === 0}
                        className="flex items-center gap-2 px-4 py-2 text-slate-600 font-medium hover:text-slate-900 disabled:opacity-30 transition-opacity"
                    >
                        <ChevronLeft size={20} /> Назад
                    </button>

                    <button
                        onClick={() => setCurrentStepIndex(prev => Math.min(lesson.steps.length - 1, prev + 1))}
                        disabled={currentStepIndex === lesson.steps.length - 1}
                        className="flex items-center gap-2 px-6 py-2 bg-white border border-slate-300 text-slate-700 font-semibold rounded-lg hover:bg-slate-100 disabled:opacity-30 transition-all"
                    >
                        Далее <ChevronRight size={20} />
                    </button>
                </div>
            </div>
        </div>
    );
};