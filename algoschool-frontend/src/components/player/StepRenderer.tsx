import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { useLessonStore } from '../../store/useLessonStore';
import type { AnyStep, TheoryStep, CodeProblemStep, InputProblemStep, ChoiceProblemStep } from './playerTypes';

import { TheoryStepDisplay } from './steps/TheoryStepDisplay';
import { CodeStepDisplay } from './steps/CodeStepDisplay';
import { InputStepDisplay } from './steps/InputStepDisplay';
import { ChoiceStepDisplay } from './steps/ChoiceStepDisplay';
// 1. ДОБАВЛЯЕМ ИМПОРТ ИСТОРИИ
import { SubmissionHistory } from './SubmissionHistory';

export interface AssessmentResult {
    status: string;
    message: string;
}

interface StepRendererProps {
    step: AnyStep;
}

export const StepRenderer: React.FC<StepRendererProps> = ({ step }) => {
    const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
    const [isLoading, setIsLoading] = useState(false);

    // 2. ДОБАВЛЯЕМ КЛЮЧ ОБНОВЛЕНИЯ
    const [refreshKey, setRefreshKey] = useState(0);
    const addCompletedStep = useLessonStore((state) => state.addCompletedStep);

    const handleUniversalSubmit = async (payload: string): Promise<AssessmentResult | null> => {
        if (!courseId || !lessonId) return null;

        setIsLoading(true);
        try {
            if (step.stepType === 'THEORY') {
                await apiClient.post(`/courses/${courseId}/lessons/${lessonId}/steps/${step.id}/read`);
                addCompletedStep(step.id);
                return { status: 'CORRECT', message: 'Материал успешно изучен!' };
            }

            const response = await apiClient.post<AssessmentResult>(
                `/courses/${courseId}/lessons/${lessonId}/steps/${step.id}/submit`,
                { payload: payload } // Отправляем payload
            );

            if (response.data.status === 'CORRECT') {
                addCompletedStep(step.id);
            }

            // 3. ОБНОВЛЯЕМ КЛЮЧ, ЧТОБЫ ИСТОРИЯ ПЕРЕЗАГРУЗИЛАСЬ
            setRefreshKey(prev => prev + 1);

            return response.data;
        } catch (err: any) {
            console.error("Ошибка при отправке решения:", err);
            alert(err.response?.data?.message || "Произошла ошибка при проверке решения.");
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    // 4. ВЫНОСИМ SWITCH В ОТДЕЛЬНУЮ ФУНКЦИЮ
    const renderStepContent = () => {
        switch (step.stepType) {
            case 'THEORY':
                return <TheoryStepDisplay step={step as TheoryStep} onSubmit={handleUniversalSubmit} isLoading={isLoading} />;
            case 'CODE_PROBLEM':
                return <CodeStepDisplay step={step as CodeProblemStep} onSubmit={handleUniversalSubmit} isLoading={isLoading} />;
            case 'INPUT_PROBLEM':
                return <InputStepDisplay step={step as InputProblemStep} onSubmit={handleUniversalSubmit} isLoading={isLoading} />;
            case 'CHOICE_PROBLEM':
                return <ChoiceStepDisplay step={step as ChoiceProblemStep} onSubmit={handleUniversalSubmit} isLoading={isLoading} />;
            default:
                return <div className="p-8 text-red-500 bg-red-50 rounded-xl">Неизвестный тип шага: {(step as any).stepType}</div>;
        }
    };

    // 5. ГЛАВНЫЙ RETURN
    return (
        <div className="flex flex-col h-full">
            {/* Отрисовываем сам шаг (Теория, Тест, Код и т.д.) */}
            <div className="flex-grow">
                {renderStepContent()}
            </div>

            {/* Отрисовываем историю только если это не теория */}
            {step.stepType !== 'THEORY' && (
                <SubmissionHistory stepId={step.id} refreshKey={refreshKey} />
            )}
        </div>
    );
};