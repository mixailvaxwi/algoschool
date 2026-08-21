import React, { useState } from 'react';
import { buildMatchingPayload, type MatchingProblemStep } from '../playerTypes';
import { Link2, Send } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';
import { ResultBanner } from './ResultBanner';

interface Props {
    step: MatchingProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

export const MatchingStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    // Ключ — индекс левого элемента, значение — индекс выбранного правого
    // в том порядке, в каком его показал сервер.
    const [choices, setChoices] = useState<Record<number, number | undefined>>({});
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const isAccepted = result?.status === 'CORRECT';
    const answered = Object.values(choices).filter((value) => value !== undefined).length;

    // Один правый элемент нельзя поставить в пару дважды: подсвечиваем занятые,
    // чтобы студент видел конфликт до отправки.
    const usedRight = new Set(Object.values(choices).filter((value) => value !== undefined));

    const handleSubmit = async () => {
        setResult(null);
        const res = await onSubmit(buildMatchingPayload(choices));
        if (res) setResult(res);
    };

    return (
        <div className="space-y-6 flex flex-col h-full">
            <div className="flex items-center gap-3 text-teal-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <Link2 size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Установите соответствие</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg mb-2">{step.description}</div>

            <div className="space-y-3 flex-1">
                {step.leftItems.map((left, leftIndex) => (
                    <div
                        key={leftIndex}
                        className="flex flex-col sm:flex-row sm:items-center gap-3 p-3 border-2 border-slate-200 rounded-xl bg-white"
                    >
                        <span className="flex-1 text-lg text-slate-800">{left}</span>
                        <select
                            value={choices[leftIndex] ?? ''}
                            disabled={isLoading || isAccepted}
                            onChange={(e) =>
                                setChoices((prev) => ({
                                    ...prev,
                                    [leftIndex]: e.target.value === '' ? undefined : Number(e.target.value),
                                }))
                            }
                            className="sm:w-72 p-3 border-2 border-slate-200 focus:border-teal-500 rounded-xl outline-none bg-slate-50 focus:bg-white transition-colors disabled:opacity-60"
                        >
                            <option value="">— выберите —</option>
                            {step.rightItems.map((right, rightIndex) => (
                                <option key={rightIndex} value={rightIndex}>
                                    {right}
                                    {usedRight.has(rightIndex) && choices[leftIndex] !== rightIndex ? ' (уже выбран)' : ''}
                                </option>
                            ))}
                        </select>
                    </div>
                ))}
            </div>

            <div className="mt-8 pt-6 border-t border-slate-100 shrink-0">
                <ResultBanner result={result} />
                <div className="flex items-center gap-4">
                    <button
                        onClick={handleSubmit}
                        disabled={answered === 0 || isLoading || isAccepted}
                        className="px-8 py-3 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                    >
                        {isLoading ? 'Проверяем...' : <><Send size={20} /> Отправить решение</>}
                    </button>
                    <span className="text-sm text-slate-500">
                        Сопоставлено {answered} из {step.leftItems.length}
                    </span>
                </div>
            </div>
        </div>
    );
};
