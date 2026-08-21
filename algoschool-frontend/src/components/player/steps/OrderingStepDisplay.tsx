import React, { useState } from 'react';
import { buildOrderingPayload, type OrderingProblemStep } from '../playerTypes';
import { ArrowUpDown, ChevronUp, ChevronDown, Send } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';
import { ResultBanner } from './ResultBanner';

interface Props {
    step: OrderingProblemStep;
    onSubmit: (payload: string) => Promise<AssessmentResult | null>;
    isLoading: boolean;
}

export const OrderingStepDisplay: React.FC<Props> = ({ step, onSubmit, isLoading }) => {
    // Храним индексы показанных элементов: сервер ждёт именно их, а тексты
    // могут повторяться и различать элементы не годятся.
    const [order, setOrder] = useState<number[]>(() => step.items.map((_, index) => index));
    const [result, setResult] = useState<AssessmentResult | null>(null);

    const isAccepted = result?.status === 'CORRECT';

    const move = (position: number, delta: number) => {
        const target = position + delta;
        if (target < 0 || target >= order.length) return;
        const next = [...order];
        [next[position], next[target]] = [next[target], next[position]];
        setOrder(next);
    };

    const handleSubmit = async () => {
        setResult(null);
        const res = await onSubmit(buildOrderingPayload(order));
        if (res) setResult(res);
    };

    return (
        <div className="space-y-6 flex flex-col h-full">
            <div className="flex items-center gap-3 text-violet-600 mb-6 pb-4 border-b border-slate-100 shrink-0">
                <ArrowUpDown size={28} />
                <h1 className="m-0 text-3xl font-bold text-slate-900">Расставьте по порядку</h1>
            </div>

            <div className="text-slate-700 whitespace-pre-wrap leading-relaxed text-lg mb-2">{step.description}</div>

            <div className="space-y-2 flex-1">
                {order.map((itemIndex, position) => (
                    <div
                        key={itemIndex}
                        className="flex items-center gap-3 p-3 border-2 border-slate-200 rounded-xl bg-white"
                    >
                        <span className="w-8 shrink-0 text-center font-mono text-slate-400 tabular-nums">
                            {position + 1}
                        </span>
                        <span className="flex-1 text-lg text-slate-800">{step.items[itemIndex]}</span>
                        <div className="flex flex-col shrink-0">
                            <button
                                onClick={() => move(position, -1)}
                                disabled={position === 0 || isLoading || isAccepted}
                                aria-label="Переместить выше"
                                className="p-1 text-slate-400 hover:text-violet-600 disabled:opacity-30 transition-colors"
                            >
                                <ChevronUp size={18} />
                            </button>
                            <button
                                onClick={() => move(position, 1)}
                                disabled={position === order.length - 1 || isLoading || isAccepted}
                                aria-label="Переместить ниже"
                                className="p-1 text-slate-400 hover:text-violet-600 disabled:opacity-30 transition-colors"
                            >
                                <ChevronDown size={18} />
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-8 pt-6 border-t border-slate-100 shrink-0">
                <ResultBanner result={result} />
                <button
                    onClick={handleSubmit}
                    disabled={isLoading || isAccepted}
                    className="w-full sm:w-auto px-8 py-3 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                >
                    {isLoading ? 'Проверяем...' : <><Send size={20} /> Отправить решение</>}
                </button>
            </div>
        </div>
    );
};
