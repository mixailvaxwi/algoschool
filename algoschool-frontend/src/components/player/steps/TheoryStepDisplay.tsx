import type {TheoryStep} from '../playerTypes';
import { FileText } from 'lucide-react';

export const TheoryStepDisplay: React.FC<{ step: TheoryStep }> = ({ step }) => (
    <div className="prose prose-slate max-w-none"> {/* prose - класс для стилизации текста */}
        <div className="flex items-center gap-3 text-blue-600 mb-6 pb-4 border-b border-slate-100">
            <FileText size={28} />
            <h1 className="m-0 text-3xl font-bold text-slate-900">Теоретический материал</h1>
        </div>
        {/* В будущем здесь будет рендеринг Markdown. Пока просто текст */}
        <div className="text-slate-700 whitespace-pre-wrap leading-relaxed">
            {step.content}
        </div>
    </div>
);