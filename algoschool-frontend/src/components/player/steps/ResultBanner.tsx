import React from 'react';
import { CheckCircle, XCircle, Clock, CircleDashed } from 'lucide-react';
import type { AssessmentResult } from '../StepRenderer';

/**
 * Итог проверки одной попытки.
 * <p>
 * Вынесен из экранов шагов: исходов стало шесть (верно, частично, неверно,
 * ждёт Ejudge, ждёт преподавателя, сбой отправки), и повторять эту развилку в
 * каждом типе задачи значило бы получить шесть разных её версий.
 */
export const ResultBanner: React.FC<{ result: AssessmentResult | null }> = ({ result }) => {
    if (!result) return null;

    const { tone, icon, title } = describe(result);

    return (
        <div className={`mb-6 p-4 rounded-xl flex items-start gap-3 border ${tone}`}>
            {icon}
            <div>
                <p className="font-bold text-lg">{title}</p>
                <p className="opacity-90">{result.message}</p>
                {result.score !== null && result.score !== undefined && result.maxScore ? (
                    <p className="opacity-90 mt-1 tabular-nums">
                        Балл: {result.score} из {result.maxScore}
                    </p>
                ) : null}
            </div>
        </div>
    );
};

const describe = (result: AssessmentResult) => {
    switch (result.status) {
        case 'CORRECT':
            return {
                tone: 'bg-emerald-50 border-emerald-200 text-emerald-800',
                icon: <CheckCircle className="shrink-0 mt-0.5" />,
                title: 'Отличная работа!',
            };
        case 'PARTIALLY_CORRECT':
            return {
                tone: 'bg-amber-50 border-amber-200 text-amber-800',
                icon: <CircleDashed className="shrink-0 mt-0.5" />,
                title: 'Частично верно',
            };
        case 'PENDING':
            return {
                tone: 'bg-slate-50 border-slate-200 text-slate-700',
                icon: <Clock className="shrink-0 mt-0.5" />,
                title: 'Проверяется',
            };
        case 'PENDING_REVIEW':
            return {
                tone: 'bg-blue-50 border-blue-200 text-blue-800',
                icon: <Clock className="shrink-0 mt-0.5" />,
                title: 'Ждёт проверки преподавателем',
            };
        case 'SUBMISSION_FAILED':
            return {
                tone: 'bg-slate-50 border-slate-200 text-slate-700',
                icon: <XCircle className="shrink-0 mt-0.5" />,
                title: 'Не удалось отправить',
            };
        default:
            return {
                tone: 'bg-red-50 border-red-200 text-red-800',
                icon: <XCircle className="shrink-0 mt-0.5" />,
                title: 'Не совсем',
            };
    }
};
