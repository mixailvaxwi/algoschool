import { useState } from 'react';
import { useParams } from 'react-router-dom';
import type {InputProblemStep} from '../playerTypes';
import { Type, Send, CheckCircle, XCircle } from 'lucide-react';
import { apiClient } from '../../../api/axios';
import { useLessonStore } from '../../../store/useLessonStore';
import { useAuthStore } from '../../../store/authStore';

interface AssessmentResult {
    isCorrect: boolean;
    message: string;
    xpAwarded: number;
}

export const InputStepDisplay: React.FC<{ step: InputProblemStep }> = ({ step }) => {
    const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
    const addCompletedStep = useLessonStore((state) => state.addCompletedStep);
    const updateXp = useAuthStore((state) => state.updateXp);

    const [answer, setAnswer] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleSubmit = async () => {
        if (!answer.trim()) return;
        setIsSubmitting(true);
        setResult(null);

        try {
            const response = await apiClient.post<AssessmentResult>(
                `/courses/${courseId}/lessons/${lessonId}/steps/${step.id}/submit`,
                { answer }
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
            <div className="flex items-center gap-3 text-emerald-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <Type size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Заполните пропуск</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg">
                {step.description}
            </div>

            <div className="mt-auto pt-8 border-t border-slate-100">
                {result && (
                    <div className={`mb-6 p-4 rounded-xl flex items-start gap-3 border ${
                        result.isCorrect ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'
                    }`}>
                        {result.isCorrect ? <CheckCircle className="shrink-0 mt-0.5" /> : <XCircle className="shrink-0 mt-0.5" />}
                        <div>
                            <p className="font-bold text-lg">{result.isCorrect ? 'Верно!' : 'Ошибка'}</p>
                            <p className="opacity-90">{result.message}</p>
                            {result.xpAwarded > 0 && (
                                <p className="mt-2 font-bold text-emerald-600">+ {result.xpAwarded} XP получено!</p>
                            )}
                        </div>
                    </div>
                )}

                <label className="block text-sm font-medium text-slate-700 mb-3">Ваш ответ:</label>
                <div className="flex gap-4">
                    <input
                        type="text"
                        value={answer}
                        onChange={(e) => setAnswer(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                        disabled={isSubmitting || result?.isCorrect}
                        className="flex-1 px-4 py-3 border-2 border-slate-200 focus:border-emerald-500 rounded-xl outline-none text-lg transition-colors bg-slate-50 focus:bg-white disabled:opacity-60"
                        placeholder="Введите точный ответ..."
                    />
                    <button
                        onClick={handleSubmit}
                        disabled={!answer.trim() || isSubmitting || result?.isCorrect}
                        className="px-8 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                    >
                        {isSubmitting ? 'Проверка...' : <><Send size={20} /> Отправить</>}
                    </button>
                </div>
            </div>
        </div>
    );
};