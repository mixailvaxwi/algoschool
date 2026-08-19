import React, { useState } from 'react';
import type { ChoiceProblemStep } from '../playerTypes';
import { CheckSquare, Send, CheckCircle, XCircle } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';

interface Props {
    step: ChoiceProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

export const ChoiceStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const handleSubmit = async () => {
        if (selectedIndex === null) return;
        setResult(null);
        const res = await onSubmit(selectedIndex.toString());
        if (res) setResult(res);
    };

    const isAccepted = result?.status === 'CORRECT';

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
                            isAccepted ? 'cursor-default opacity-80' : 'cursor-pointer hover:border-blue-300 hover:bg-slate-50'
                        } ${
                            selectedIndex === index ? 'border-blue-500 bg-blue-50 shadow-sm' : 'border-slate-200'
                        }`}
                    >
                        <input
                            type="radio"
                            name={`choice-${step.id}`}
                            value={index}
                            checked={selectedIndex === index}
                            onChange={() => !isAccepted && setSelectedIndex(index)}
                            disabled={isLoading || isAccepted}
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
                        isAccepted ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'
                    }`}>
                        {isAccepted ? <CheckCircle className="shrink-0 mt-0.5" /> : <XCircle className="shrink-0 mt-0.5" />}
                        <div>
                            <p className="font-bold text-lg">{isAccepted ? 'Отличная работа!' : 'Не совсем'}</p>
                            <p className="opacity-90">{result.message}</p>
                        </div>
                    </div>
                )}
                <button
                    onClick={handleSubmit}
                    disabled={selectedIndex === null || isLoading || isAccepted}
                    className="w-full sm:w-auto px-8 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                >
                    {isLoading ? 'Проверяем...' : <><Send size={20} /> Отправить решение</>}
                </button>
            </div>
        </div>
    );
};