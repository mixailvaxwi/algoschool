import React, { useState } from 'react';
import type { InputProblemStep } from '../playerTypes';
import { Type, Send, CheckCircle, XCircle } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';

interface Props {
    step: InputProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

export const InputStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    const [answer, setAnswer] = useState('');
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleSubmit = async () => {
        if (!answer.trim()) return;
        setResult(null);
        const res = await onSubmit(answer);
        if (res) setResult(res);
    };

    const isAccepted = result?.status === 'ACCEPTED';

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
                        isAccepted ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'
                    }`}>
                        {isAccepted ? <CheckCircle className="shrink-0 mt-0.5" /> : <XCircle className="shrink-0 mt-0.5" />}
                        <div>
                            <p className="font-bold text-lg">{isAccepted ? 'Верно!' : 'Ошибка'}</p>
                            <p className="opacity-90">{result.message}</p>
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
                        disabled={isLoading || isAccepted}
                        className="flex-1 px-4 py-3 border-2 border-slate-200 focus:border-emerald-500 rounded-xl outline-none text-lg transition-colors bg-slate-50 focus:bg-white disabled:opacity-60"
                        placeholder="Введите точный ответ..."
                    />
                    <button
                        onClick={handleSubmit}
                        disabled={!answer.trim() || isLoading || isAccepted}
                        className="px-8 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center gap-2"
                    >
                        {isLoading ? 'Проверка...' : <><Send size={20} /> Отправить</>}
                    </button>
                </div>
            </div>
        </div>
    );
};