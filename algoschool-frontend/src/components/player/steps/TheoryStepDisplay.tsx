import React, { useState } from 'react';
import type { TheoryStep } from '../playerTypes';
import { FileText, CheckCircle } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';

interface Props {
    step: TheoryStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

export const TheoryStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleComplete = async () => {
        const res = await onSubmit("READ"); // Теория отправляет просто метку прочтения
        if (res) setResult(res);
    };

    const isAccepted = result?.status === 'CORRECT';

    return (
        <div className="prose prose-slate max-w-none flex flex-col h-full">
            <div className="flex items-center gap-3 text-blue-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <FileText size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Теоретический материал</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed flex-grow">
                {step.content}
            </div>

            <div className="mt-8 pt-6 border-t border-slate-100 shrink-0">
                {isAccepted ? (
                    <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl flex items-center gap-3 font-medium">
                        <CheckCircle /> Материал изучен!
                    </div>
                ) : (
                    <button
                        onClick={handleComplete}
                        disabled={isLoading}
                        className="px-8 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50"
                    >
                        {isLoading ? "Обработка..." : "Завершить шаг"}
                    </button>
                )}
            </div>
        </div>
    );
};