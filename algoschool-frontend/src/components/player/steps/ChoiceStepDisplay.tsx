import { useLessonStore } from '../../../store/useLessonStore';
import { useAuthStore } from '../../../store/authStore';
import { useState } from 'react';
import { useParams } from 'react-router-dom';
import type {ChoiceProblemStep} from '../playerTypes';
import { CheckSquare, Send, CheckCircle, XCircle } from 'lucide-react';
import { apiClient } from '../../../api/axios';

interface AssessmentResult {
    isCorrect: boolean;
    message: string;
    xpAwarded: number;
}

export const ChoiceStepDisplay: React.FC<{ step: ChoiceProblemStep }> = ({ step }) => {
    const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
    const addCompletedStep = useLessonStore((state) => state.addCompletedStep);
    const updateXp = useAuthStore((state) => state.updateXp);

    const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleSubmit = async () => {
        if (selectedIndex === null) return;
        setIsSubmitting(true);
        setResult(null);

        try {
            const response = await apiClient.post<AssessmentResult>(
                `/courses/${courseId}/lessons/${lessonId}/steps/${step.id}/submit`,
                { answer: selectedIndex.toString() }
            );
            setResult(response.data);

            if (response.data.isCorrect) {
                addCompletedStep(step.id);
                if (response.data.xpAwarded > 0) {
                    updateXp(response.data.xpAwarded);
                }
            }
        } catch (error: any) {
            console.error(error);
            alert('Ошибка связи с сервером!');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="space-y-6 flex flex-col h-full">
            <div className="flex items-center gap-3 text-blue-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <CheckSquare size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Выберите правильный ответ</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg mb-4">
                {step.description}
            </div>

            <div className="space-y-3 flex-1">
                {step.options?.map((option, index) => (
                    <label
                        key={index}
                        className={`flex items-center gap-4 p-4 border-2 rounded-xl transition-all ${
                            result?.isCorrect ? 'cursor-default opacity-80' : 'cursor-pointer hover:border-blue-300 hover:bg-slate-50'
                        } ${
                            selectedIndex === index ? 'border-blue-500 bg-blue-50 shadow-sm' : 'border-slate-200'
                        }`}
                    >
                        <input
                            type="radio"
                            name={`choice-${step.id}`}
                            value={index}
                            checked={selectedIndex === index}
                            onChange={() => !result?.isCorrect && setSelectedIndex(index)}
                            disabled={isSubmitting || result?.isCorrect}
                            className="w-5 h-5 text-blue-600 focus:ring-blue-500 border-slate-300 cursor-pointer disabled:cursor-default"
                        />
                        <span className={`text-lg ${selectedIndex === index ? 'text-blue-900 font-medium' : 'text-slate-700'}`}>
                            {option}
                        </span>
                    </label>
                ))}
            </div>

            <div className="mt-8 pt-6 border-t border-slate-100 shrink-0">
                {result && (
                    <div className={`mb-6 p-4 rounded-xl flex items-start gap-3 border ${
                        result.isCorrect ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'
                    }`}>
                        {result.isCorrect ? <CheckCircle className="shrink-0 mt-0.5" /> : <XCircle className="shrink-0 mt-0.5" />}
                        <div>
                            <p className="font-bold text-lg">{result.isCorrect ? 'Отличная работа!' : 'Не совсем'}</p>
                            <p className="opacity-90">{result.message}</p>
                            {result.xpAwarded > 0 && <p className="mt-2 font-bold text-emerald-600">+ {result.xpAwarded} XP</p>}
                        </div>
                    </div>
                )}

                <button
                    onClick={handleSubmit}
                    disabled={selectedIndex === null || isSubmitting || result?.isCorrect}
                    className="w-full sm:w-auto px-8 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                    {isSubmitting ? 'Проверяем...' : <><Send size={20} /> Отправить решение</>}
                </button>
            </div>
        </div>
    );
};