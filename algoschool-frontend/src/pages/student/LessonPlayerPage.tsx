import { useLessonStore } from '../../store/useLessonStore';
import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ChevronLeft, Menu, CheckCircle, Circle, PlayCircle, Target } from 'lucide-react';
import { apiClient } from '../../api/axios';
import type { FullLesson, AnyStep } from '../../components/player/playerTypes';
import { StepRenderer } from '../../components/player/StepRenderer';

// Обновляем типы для сайдбара в соответствии с тем, что приходит от API
interface LessonPreview {
    id: number;
    title: string;
    orderIndex: number;
}
interface ModulePreview {
    id: number;
    title: string;
    orderIndex: number;
    lessons: LessonPreview[];
}

export const LessonPlayerPage = () => {
    const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
    const navigate = useNavigate();

    const completedSteps = useLessonStore((state) => state.completedSteps);
    const setInitialCompletedSteps = useLessonStore((state) => state.setInitialCompletedSteps);
    const resetLessonProgress = useLessonStore((state) => state.resetLessonProgress);

    useEffect(() => {
        resetLessonProgress();
    }, [lessonId, resetLessonProgress]);

    const [lesson, setLesson] = useState<FullLesson | null>(null);
    const [modules, setModules] = useState<ModulePreview[]>([]); // Состояние для структуры курса
    const [currentStepIndex, setCurrentStepIndex] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);

    // Загрузка данных урока И структуры курса
    useEffect(() => {
        const fetchLessonData = async () => {
            setIsLoading(true);
            setError(null);
            try {
                // Используем Promise.all для параллельной загрузки
                const [lessonResponse, structureResponse] = await Promise.all([
                    // ВАЖНО: Указываем, что ждем массив number[], а не Set
                    apiClient.get<{ lesson: FullLesson, completedSteps: number[] }>(`/courses/${courseId}/lessons/${lessonId}`),
                    apiClient.get<ModulePreview[]>(`/courses/${courseId}/structure`)
                ]);

                const { lesson: lessonData, completedSteps: completedStepsArray } = lessonResponse.data;
                lessonData.steps.sort((a, b) => a.orderIndex - b.orderIndex);

                setLesson(lessonData);
                // Превращаем массив в Set перед отправкой в Store!
                setInitialCompletedSteps(new Set(completedStepsArray || []));
                setCurrentStepIndex(0);

                // Сохраняем структуру курса для сайдбара
                setModules(structureResponse.data);

            } catch (err: any) {
                console.error(err);
                setError(err.response?.data?.message || 'Не удалось загрузить урок или его структуру');
            } finally {
                setIsLoading(false);
            }
        };

        if (lessonId && courseId) {
            fetchLessonData();
        }
    }, [courseId, lessonId, setInitialCompletedSteps]);


    if (isLoading) return <div className="h-screen flex items-center justify-center bg-slate-50 text-slate-500 font-medium">Загрузка содержимого урока...</div>;
    if (error || !lesson) return <div className="h-screen flex items-center justify-center bg-slate-50 text-red-500 font-bold p-10 text-center">{error || 'Урок не найден'}</div>;

    const currentStep: AnyStep | undefined = lesson.steps[currentStepIndex];

    return (
        <div className="flex h-screen bg-slate-50 overflow-hidden font-sans">

            {/* --- БОКОВАЯ ПАНЕЛЬ --- */}
            <div className={`${isSidebarOpen ? 'w-80' : 'w-0'} shrink-0 bg-white border-r border-slate-200 transition-all duration-300 overflow-hidden flex flex-col z-20`}>
                <div className="h-16 flex items-center px-4 border-b border-slate-200 bg-slate-50 shrink-0">
                    <Link to={`/courses/${courseId}`} className="flex items-center gap-2 text-slate-600 hover:text-blue-600 transition-colors font-medium">
                        <ChevronLeft size={20} /> К оглавлению курса
                    </Link>
                </div>
                <div className="flex-1 overflow-y-auto p-4 space-y-6">
                    {modules.map(module => (
                        <div key={module.id}>
                            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Модуль {module.orderIndex}. {module.title}</h3>
                            <div className="space-y-1">
                                {module.lessons.map(lessonInSidebar => {
                                    const isActive = lessonInSidebar.id === Number(lessonId);
                                    // TODO: В будущем можно вычислять isCompleted для урока, если все его шаги есть в completedSteps
                                    return (
                                        <button key={lessonInSidebar.id} onClick={() => navigate(`/courses/${courseId}/lessons/${lessonInSidebar.id}`)} className={`w-full flex items-start gap-3 p-2 rounded-lg text-left transition-colors ${isActive ? 'bg-blue-50 text-blue-700' : 'hover:bg-slate-100 text-slate-700'}`}>
                                            <div className="mt-0.5">{isActive ? <PlayCircle size={18} className="text-blue-600" /> : <Circle size={18} className="text-slate-300" />}</div>
                                            <span className={`text-sm ${isActive ? 'font-semibold' : 'font-medium'}`}>{lessonInSidebar.orderIndex}. {lessonInSidebar.title}</span>
                                        </button>
                                    );
                                })}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* --- ОСНОВНАЯ ОБЛАСТЬ --- */}
            <div className="flex-1 flex flex-col min-w-0">
                <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 lg:px-8 shrink-0 relative z-10">
                    <div className="flex items-center gap-4">
                        <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="p-2 text-slate-500 hover:bg-slate-100 rounded-lg transition-colors"><Menu size={20} /></button>
                        <h1 className="text-lg font-bold text-slate-800 hidden sm:block">{lesson.title}</h1>
                    </div>

                    <div className="flex items-center gap-2">
                        {lesson.steps.map((step, index) => {
                            const isCompleted = completedSteps.has(step.id);
                            const isActive = index === currentStepIndex;

                            return (
                                <button
                                    key={step.id}
                                    onClick={() => setCurrentStepIndex(index)}
                                    title={`Шаг ${index + 1}`}
                                    className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-all duration-300 ${
                                        isActive && !isCompleted ? 'bg-blue-600 text-white shadow-md'
                                            : isCompleted ? 'bg-emerald-500 text-white shadow-md transform scale-105'
                                                : 'bg-slate-100 text-slate-500 border border-slate-200 hover:bg-slate-200'
                                    }`}
                                >
                                    {isCompleted ? <CheckCircle size={16} /> : (index + 1)}
                                </button>
                            );
                        })}
                    </div>
                </header>

                <main className="flex-1 overflow-y-auto bg-slate-50 p-4 lg:p-8">
                    <div className="max-w-4xl mx-auto h-full flex flex-col">
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8 flex-1">
                            {currentStep ? (
                                <StepRenderer step={currentStep} />
                            ) : (
                                <div className="text-center text-slate-400 py-20 border-2 border-dashed rounded-xl border-slate-200">
                                    <Target size={48} className="mx-auto mb-4"/>
                                    В этом уроке пока нет шагов.
                                </div>
                            )}
                        </div>

                        <div className="mt-8 flex gap-4 shrink-0 pb-4">
                            <button
                                onClick={() => setCurrentStepIndex(prev => prev - 1)}
                                disabled={currentStepIndex === 0}
                                className="px-6 py-2 bg-white border border-slate-300 hover:bg-slate-100 text-slate-700 font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Назад
                            </button>
                            <button
                                onClick={() => setCurrentStepIndex(prev => prev + 1)}
                                disabled={currentStepIndex === lesson.steps.length - 1}
                                className="ml-auto px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Вперед
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};