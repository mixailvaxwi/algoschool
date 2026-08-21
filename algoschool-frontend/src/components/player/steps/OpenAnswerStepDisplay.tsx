import React, { useState } from 'react';
import type { OpenAnswerProblemStep } from '../playerTypes';
import { PenLine, Send } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';
import { ResultBanner } from './ResultBanner';

interface Props {
    step: OpenAnswerProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

/**
 * Развёрнутый ответ. Проверяет человек, поэтому отправка не даёт вердикта
 * сразу — ответ уходит в очередь к преподавателю, и балл появится позже
 * в истории попыток.
 */
export const OpenAnswerStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    const [answer, setAnswer] = useState('');
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleSubmit = async () => {
        if (!answer.trim()) return;
        setResult(null);
        const res = await onSubmit(answer);
        if (res) {
            setResult(res);
            setAnswer('');
        }
    };

    return (
        <div className="space-y-6 flex flex-col h-full">
            <div className="flex items-center gap-3 text-rose-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <PenLine size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Развёрнутый ответ</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg">{step.description}</div>

            <div className="mt-auto pt-8 border-t border-slate-100">
                <ResultBanner result={result} />

                <label className="block text-sm font-medium text-slate-700 mb-1">Ваш ответ:</label>
                <p className="text-sm text-slate-500 mb-3">
                    Ответ читает преподаватель — вердикт появится в истории попыток вместе с комментарием.
                </p>
                <textarea
                    value={answer}
                    onChange={(e) => setAnswer(e.target.value)}
                    disabled={isLoading}
                    className="w-full h-48 px-4 py-3 border-2 border-slate-200 focus:border-rose-500 rounded-xl outline-none text-lg leading-relaxed transition-colors bg-slate-50 focus:bg-white resize-y disabled:opacity-60"
                    placeholder="Изложите решение своими словами..."
                />
                <button
                    onClick={handleSubmit}
                    disabled={!answer.trim() || isLoading}
                    className="mt-4 px-8 py-3 bg-rose-600 hover:bg-rose-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center gap-2"
                >
                    {isLoading ? 'Отправляем...' : <><Send size={20} /> Отправить на проверку</>}
                </button>
            </div>
        </div>
    );
};
