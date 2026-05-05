import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { useAuthStore } from '../store/authStore';
import { CheckCircle2, XCircle, Clock, Code2, AlertCircle, ChevronLeft, ChevronRight, BookOpen } from 'lucide-react';
import type { Step } from '../types';

interface Lesson {
    id: number;
    title: string;
    steps: Step[];
}

const StepRenderer = ({ step }: { step: Step }) => {
    const [payload, setPayload] = useState('');
    const [status, setStatus] = useState<'IDLE' | 'LOADING' | 'CORRECT' | 'WRONG_ANSWER' | 'PENDING' | 'ERROR'>('IDLE');
    const [xpEarned, setXpEarned] = useState(0);
    const { updateXp } = useAuthStore();

    useEffect(() => {
        setPayload('');
        setStatus('IDLE');
        setXpEarned(0);
    }, [step.id]);

    const handleSubmit = async () => {
        if (!payload.trim() && step.stepType !== 'THEORY') return;

        setStatus('LOADING');
        try {
            const response = await apiClient.post('/submissions', {
                problemId: step.id,
                payload: payload
            });

            const data = response.data;
            setStatus(data.status);
            setXpEarned(data.xpEarned);

            if (data.xpEarned > 0) {
                updateXp(data.xpEarned);
            }
        } catch (error) {
            console.error(error);
            setStatus('ERROR');
        }
    };

    const renderVerdict = () => {
        if (status === 'IDLE' || status === 'LOADING') return null;

        if (status === 'CORRECT') {
            return (
                <div className="mt-4 p-4 bg-emerald-50 border border-emerald-200 rounded-lg flex flex-col gap-2">
                    <div className="flex items-center gap-2 text-emerald-700 font-bold">
                        <CheckCircle2 size={20} /> Верное решение!
                    </div>
                    {xpEarned > 0 && (
                        <span className="text-emerald-600 text-sm">Вам начислено +{xpEarned} XP</span>
                    )}
                </div>
            );
        }

        if (status === 'WRONG_ANSWER') {
            return (
                <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-700 font-bold">
                    <XCircle size={20} /> Неверный ответ, попробуйте еще раз.
                </div>
            );
        }

        if (status === 'PENDING') {
            return (
                <div className="mt-4 p-4 bg-amber-50 border border-amber-200 rounded-lg flex items-center gap-2 text-amber-700 font-bold">
                    <Clock size={20} /> Код отправлен на проверку. Подождите...
                </div>
            );
        }

        return (
            <div className="mt-4 p-4 bg-slate-50 border border-slate-200 rounded-lg flex items-center gap-2 text-slate-700 font-bold">
                <AlertCircle size={20} /> Произошла ошибка при отправке.
            </div>
        );
    };

    switch (step.stepType) {
        case 'THEORY':
            return (
                <div className="prose max-w-none text-slate-800">
                    <div dangerouslySetInnerHTML={{ __html: step.content }} />
                </div>
            );

        case 'INPUT_PROBLEM':
            return (
                <div className="space-y-6">
                    <div className="prose max-w-none text-slate-800" dangerouslySetInnerHTML={{ __html: step.description }} />
                    <div className="bg-slate-50 p-6 rounded-xl border border-slate-200 shadow-inner">
                        <label className="block text-sm font-medium text-slate-700 mb-2">Ваш ответ:</label>
                        <input
                            type="text"
                            value={payload}
                            onChange={(e) => setPayload(e.target.value)}
                            disabled={status === 'CORRECT' || status === 'LOADING'}
                            className="w-full max-w-md px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none disabled:bg-slate-100 disabled:text-slate-500 transition-all"
                            placeholder="Введите точный ответ..."
                        />
                        <button
                            onClick={handleSubmit}
                            disabled={!payload.trim() || status === 'CORRECT' || status === 'LOADING'}
                            className="mt-4 px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-300 text-white font-medium rounded-lg transition-colors flex items-center gap-2"
                        >
                            {status === 'LOADING' ? 'Проверка...' : 'Отправить решение'}
                        </button>
                        {renderVerdict()}
                    </div>
                </div>
            );

        case 'CODE_PROBLEM':
            return (
                <div className="space-y-4">
                    <div className="flex justify-between items-center bg-slate-100 px-4 py-2 rounded-lg border border-slate-200 text-sm">
                        <span className="font-medium text-slate-700">Языки: {step.allowedLanguages}</span>
                        <div className="flex gap-4 text-slate-500 font-mono">
                            <span>⏱ {step.timeLimitSec}s</span>
                            <span>💾 {step.memoryLimitMb}MB</span>
                        </div>
                    </div>
                    <div className="prose max-w-none text-slate-800" dangerouslySetInnerHTML={{ __html: step.description }} />
                    <textarea
                        value={payload}
                        onChange={(e) => setPayload(e.target.value)}
                        disabled={status === 'LOADING'}
                        className="w-full h-64 p-4 font-mono text-sm bg-slate-900 text-slate-100 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none resize-y"
                        placeholder="// Напишите ваш код здесь..."
                        spellCheck="false"
                    />
                    <button
                        onClick={handleSubmit}
                        disabled={!payload.trim() || status === 'LOADING'}
                        className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-300 text-white font-medium rounded-lg transition-colors flex items-center gap-2"
                    >
                        <Code2 size={18} /> Запустить код
                    </button>
                    {renderVerdict()}
                </div>
            );

        case 'CHOICE_PROBLEM':
            const handleToggleOption = (id: number) => {
                const currentIds = payload ? payload.split(',').map(Number) : [];
                let newIds;
                if (currentIds.includes(id)) {
                    newIds = currentIds.filter(i => i !== id);
                } else {
                    newIds = step.isMultipleChoice ? [...currentIds, id] : [id];
                }
                setPayload(newIds.join(','));
            };

            const selectedIds = payload ? payload.split(',').map(Number) : [];

            return (
                <div className="space-y-6">
                    <div className="prose max-w-none text-slate-800" dangerouslySetInnerHTML={{ __html: step.description }} />
                    <div className="bg-slate-50 p-6 rounded-xl border border-slate-200">
                        <div className="space-y-3 mb-6">
                            {step.options?.map(option => (
                                <label key={option.id} className="flex items-center gap-3 p-3 bg-white border border-slate-200 rounded-lg cursor-pointer hover:bg-blue-50 transition-colors">
                                    <input
                                        type={step.isMultipleChoice ? "checkbox" : "radio"}
                                        name={`problem_${step.id}`}
                                        checked={selectedIds.includes(option.id)}
                                        onChange={() => handleToggleOption(option.id)}
                                        disabled={status === 'CORRECT' || status === 'LOADING'}
                                        className="w-4 h-4 text-blue-600 focus:ring-blue-500"
                                    />
                                    <span className="text-slate-700">{option.text}</span>
                                </label>
                            ))}
                            {(!step.options || step.options.length === 0) && (
                                <p className="text-slate-500 italic">Варианты ответов не загружены...</p>
                            )}
                        </div>
                        <button
                            onClick={handleSubmit}
                            disabled={!payload || status === 'CORRECT' || status === 'LOADING'}
                            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-300 text-white font-medium rounded-lg transition-colors flex items-center gap-2"
                        >
                            Отправить решение
                        </button>
                        {renderVerdict()}
                    </div>
                </div>
            );

        default:
            return <div className="text-red-500 font-bold p-4 bg-red-50 rounded-lg">Неизвестный тип шага</div>;
    }
};

export const LessonPage = () => {
    const { lessonId } = useParams<{ lessonId: string }>();
    const navigate = useNavigate();
    const [lesson, setLesson] = useState<Lesson | null>(null);
    const [currentStepIndex, setCurrentStepIndex] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchLesson = async () => {
            try {
                const response = await apiClient.get(`/lessons/${lessonId}`);
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
