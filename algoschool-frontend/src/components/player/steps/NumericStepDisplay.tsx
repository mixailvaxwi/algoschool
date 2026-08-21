import React, { useState } from 'react';
import type { NumericProblemStep } from '../playerTypes';
import { Sigma, Send } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';
import { ResultBanner } from './ResultBanner';

interface Props {
    step: NumericProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

/** Подсказка о требуемой точности: без неё непонятно, до скольких знаков округлять. */
const toleranceHint = (step: NumericProblemStep): string | null => {
    if (!step.tolerance) return 'Ответ должен совпасть точно.';
    return step.toleranceKind === 'RELATIVE'
        ? `Допустимая относительная погрешность: ${step.tolerance * 100}%.`
        : `Допустимая погрешность: ±${step.tolerance}.`;
};

export const NumericStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    const [answer, setAnswer] = useState('');
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleSubmit = async () => {
        if (!answer.trim()) return;
        setResult(null);
        const res = await onSubmit(answer);
        if (res) setResult(res);
    };

    const isAccepted = result?.status === 'CORRECT';

    return (
        <div className="space-y-6 flex flex-col h-full">
            <div className="flex items-center gap-3 text-indigo-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <Sigma size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Числовой ответ</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg">{step.description}</div>

            <div className="mt-auto pt-8 border-t border-slate-100">
                <ResultBanner result={result} />

                <label className="block text-sm font-medium text-slate-700 mb-1">Ваш ответ:</label>
                <p className="text-sm text-slate-500 mb-3">{toleranceHint(step)} Разделитель — точка или запятая.</p>
                <div className="flex gap-4">
                    <input
                        type="text"
                        inputMode="decimal"
                        value={answer}
                        onChange={(e) => setAnswer(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                        disabled={isLoading || isAccepted}
                        className="flex-1 px-4 py-3 border-2 border-slate-200 focus:border-indigo-500 rounded-xl outline-none text-lg font-mono transition-colors bg-slate-50 focus:bg-white disabled:opacity-60"
                        placeholder="Например: 3.14"
                    />
                    <button
                        onClick={handleSubmit}
                        disabled={!answer.trim() || isLoading || isAccepted}
                        className="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center gap-2"
                    >
                        {isLoading ? 'Проверка...' : <><Send size={20} /> Отправить</>}
                    </button>
                </div>
            </div>
        </div>
    );
};
