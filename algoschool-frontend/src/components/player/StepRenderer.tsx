import type {AnyStep, TheoryStep, CodeProblemStep, InputProblemStep, ChoiceProblemStep} from './playerTypes';
import { TheoryStepDisplay } from './steps/TheoryStepDisplay';
import { CodeStepDisplay } from './steps/CodeStepDisplay';
import { InputStepDisplay } from './steps/InputStepDisplay';
import { ChoiceStepDisplay } from './steps/ChoiceStepDisplay';

interface StepRendererProps {
    step: AnyStep;
}

export const StepRenderer: React.FC<StepRendererProps> = ({ step }) => {
    // В зависимости от stepType выбираем, какой компонент отрендерить
    switch (step.stepType) {
        case 'THEORY':
            return <TheoryStepDisplay step={step as TheoryStep} />;

        case 'CODE_PROBLEM':
            return <CodeStepDisplay step={step as CodeProblemStep} />;

        case 'INPUT_PROBLEM':
            return <InputStepDisplay step={step as InputProblemStep} />;

        case 'CHOICE_PROBLEM':
            return <ChoiceStepDisplay step={step as ChoiceProblemStep} />;

        default:
            return <div className="p-8 text-red-500 bg-red-50 rounded-xl">Неизвестный тип шага: {(step as any).stepType}</div>;
    }
};