import { Plus, Trash2 } from 'lucide-react';
import {
    DIFFICULTY_LABELS,
    VISIBILITY_LABELS,
    type Difficulty,
    type ProblemContent,
    type ProblemType,
    type Visibility,
} from '../../types/problem';

interface Props {
    type: ProblemType;
    value: ProblemContent;
    onChange: (next: ProblemContent) => void;
    /** Чужая публичная задача: поставить в урок можно, править содержание — нет. */
    disabled?: boolean;
}

/**
 * Форма содержания задачи.
 * <p>
 * Одна и та же и в банке задач, и в редакторе урока: задачу можно завести
 * обоими путями, и правила (сколько вариантов, что отмечено правильным)
 * должны выглядеть одинаково — иначе формы разъедутся при первой же правке.
 */
export const ProblemFields = ({ type, value, onChange, disabled = false }: Props) => {
    const patch = (fields: Partial<ProblemContent>) => onChange({ ...value, ...fields });

    const handleOptionChange = (index: number, text: string) => {
        const options = [...value.options];
        options[index] = text;
        patch({ options });
    };

    const addOption = () => patch({ options: [...value.options, `Вариант ${value.options.length + 1}`] });

    const removeOption = (index: number) => {
        if (value.options.length <= 2) return; // Меньше двух вариантов оставлять нельзя

        // Сдвигаем индексы правильных ответов вслед за удалённым вариантом;
        // если удалили единственный правильный — подставляем первый по умолчанию.
        const shifted = value.correctOptionIndexes
            .filter((i) => i !== index)
            .map((i) => (i > index ? i - 1 : i));

        patch({
            options: value.options.filter((_, i) => i !== index),
            correctOptionIndexes: shifted.length > 0 ? shifted : [0],
        });
    };

    const toggleCorrectOption = (index: number) => {
        if (!value.isMultipleChoice) {
            patch({ correctOptionIndexes: [index] });
            return;
        }
        patch({
            correctOptionIndexes: value.correctOptionIndexes.includes(index)
                ? value.correctOptionIndexes.filter((i) => i !== index)
                : [...value.correctOptionIndexes, index],
        });
    };

    // Переключение обратно на одиночный выбор с несколькими отмеченными
    // вариантами оставило бы форму в состоянии, которое отклонит сервер.
    const toggleMultipleChoice = (checked: boolean) => {
        patch({
            isMultipleChoice: checked,
            correctOptionIndexes:
                !checked && value.correctOptionIndexes.length > 1
                    ? [value.correctOptionIndexes[0]]
                    : value.correctOptionIndexes,
        });
    };

    const inputClass =
        'w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-slate-100 disabled:text-slate-500';

    return (
        <div className="space-y-6">
            <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">Название задачи</label>
                <input
                    type="text"
                    value={value.title}
                    disabled={disabled}
                    maxLength={200}
                    onChange={(e) => patch({ title: e.target.value })}
                    placeholder="Как задача называется в банке"
                    className={inputClass}
                />
                <p className="text-xs text-slate-400 mt-1">
                    Если не заполнить, названием станет первая строка условия.
                </p>
            </div>

            <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">Условие задачи</label>
                <textarea
                    value={value.description}
                    disabled={disabled}
                    onChange={(e) => patch({ description: e.target.value })}
                    className="w-full h-40 p-4 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 resize-y disabled:text-slate-500"
                    placeholder="Напишите, что нужно сделать студенту..."
                />
            </div>

            <div className="grid grid-cols-3 gap-4">
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Сложность</label>
                    <select
                        value={value.difficulty}
                        disabled={disabled}
                        onChange={(e) => patch({ difficulty: e.target.value as '' | Difficulty })}
                        className={inputClass}
                    >
                        <option value="">Не указана</option>
                        {(Object.keys(DIFFICULTY_LABELS) as Difficulty[]).map((level) => (
                            <option key={level} value={level}>{DIFFICULTY_LABELS[level]}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Кто видит в банке</label>
                    <select
                        value={value.visibility}
                        disabled={disabled}
                        onChange={(e) => patch({ visibility: e.target.value as Visibility })}
                        className={inputClass}
                    >
                        {(Object.keys(VISIBILITY_LABELS) as Visibility[]).map((level) => (
                            <option key={level} value={level}>{VISIBILITY_LABELS[level]}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Вес (баллы)</label>
                    <input
                        type="number"
                        min={1}
                        value={value.maxScore}
                        disabled={disabled}
                        onChange={(e) => patch({ maxScore: Math.max(1, Number(e.target.value)) })}
                        className={inputClass}
                    />
                </div>
            </div>

            <div>
                <label className="block text-sm font-semibold text-slate-700 mb-2">Теги (через запятую)</label>
                <input
                    type="text"
                    value={value.tags.join(', ')}
                    disabled={disabled}
                    onChange={(e) => patch({ tags: e.target.value.split(',').map((t) => t.trim()).filter(Boolean) })}
                    placeholder="графы, dfs, олимпиада"
                    className={inputClass}
                />
            </div>

            {type === 'INPUT_PROBLEM' && (
                <div className="p-6 bg-slate-50 rounded-xl border border-slate-200">
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Правильный ответ (строка)</label>
                    <input
                        type="text"
                        value={value.correctAnswer}
                        disabled={disabled}
                        onChange={(e) => patch({ correctAnswer: e.target.value })}
                        placeholder="Например: 42"
                        className={`${inputClass} font-mono bg-white`}
                    />
                    <p className="text-sm text-slate-500 mt-2">Ответ проверяется на точное совпадение (без учёта регистра).</p>
                </div>
            )}

            {type === 'CHOICE_PROBLEM' && (
                <div className="p-6 bg-slate-50 rounded-xl border border-slate-200">
                    <label className="flex items-center gap-3 mb-6 cursor-pointer w-fit">
                        <input
                            type="checkbox"
                            checked={value.isMultipleChoice}
                            disabled={disabled}
                            onChange={(e) => toggleMultipleChoice(e.target.checked)}
                            className="w-5 h-5 text-blue-600 focus:ring-blue-500 cursor-pointer rounded"
                        />
                        <span className="text-sm font-semibold text-slate-700">Несколько правильных ответов</span>
                    </label>

                    <label className="block text-sm font-semibold text-slate-700 mb-4">
                        {value.isMultipleChoice
                            ? 'Варианты ответа (отметьте все правильные)'
                            : 'Варианты ответа (отметьте правильный)'}
                    </label>

                    <div className="space-y-3">
                        {value.options.map((option, index) => (
                            <div key={index} className="flex items-center gap-3 bg-white p-2 rounded-lg border border-slate-200">
                                <input
                                    type={value.isMultipleChoice ? 'checkbox' : 'radio'}
                                    name="correctOption"
                                    checked={value.correctOptionIndexes.includes(index)}
                                    disabled={disabled}
                                    onChange={() => toggleCorrectOption(index)}
                                    className="w-5 h-5 ml-2 text-blue-600 focus:ring-blue-500 cursor-pointer"
                                />
                                <input
                                    type="text"
                                    value={option}
                                    disabled={disabled}
                                    onChange={(e) => handleOptionChange(index, e.target.value)}
                                    className="flex-1 p-2 border-none outline-none focus:ring-0 disabled:text-slate-500"
                                    placeholder={`Вариант ${index + 1}`}
                                />
                                <button
                                    type="button"
                                    onClick={() => removeOption(index)}
                                    disabled={disabled || value.options.length <= 2}
                                    className="p-2 text-slate-400 hover:text-red-500 disabled:opacity-30 transition-colors"
                                >
                                    <Trash2 size={18} />
                                </button>
                            </div>
                        ))}
                    </div>

                    <button
                        type="button"
                        onClick={addOption}
                        disabled={disabled}
                        className="mt-4 flex items-center gap-2 text-sm font-semibold text-blue-600 hover:text-blue-700 disabled:opacity-40 transition-colors"
                    >
                        <Plus size={16} /> Добавить вариант
                    </button>
                </div>
            )}

            {type === 'CODE_PROBLEM' && (
                <div className="p-6 bg-slate-50 rounded-xl border border-slate-200 grid grid-cols-2 gap-6">
                    <div className="col-span-2 grid grid-cols-2 gap-6 p-5 bg-blue-50/50 rounded-xl border border-blue-100">
                        <h4 className="col-span-2 text-sm font-bold text-blue-800">Интеграция с Ejudge</h4>
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">ID турнира (Contest ID)</label>
                            <input
                                type="number"
                                value={value.ejudgeContestId}
                                disabled={disabled}
                                onChange={(e) => patch({ ejudgeContestId: e.target.value === '' ? '' : Number(e.target.value) })}
                                placeholder="Например: 101"
                                className={`${inputClass} bg-white`}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">ID задачи (short name)</label>
                            <input
                                type="text"
                                value={value.ejudgeProblemId}
                                disabled={disabled}
                                onChange={(e) => patch({ ejudgeProblemId: e.target.value })}
                                placeholder="Например: 1 или A"
                                className={`${inputClass} bg-white`}
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Лимит времени (сек)</label>
                        <input
                            type="number"
                            min={1}
                            value={value.timeLimitSec}
                            disabled={disabled}
                            onChange={(e) => patch({ timeLimitSec: Number(e.target.value) })}
                            className={`${inputClass} bg-white`}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Лимит памяти (МБ)</label>
                        <input
                            type="number"
                            min={16}
                            value={value.memoryLimitMb}
                            disabled={disabled}
                            onChange={(e) => patch({ memoryLimitMb: Number(e.target.value) })}
                            className={`${inputClass} bg-white`}
                        />
                    </div>
                    <div className="col-span-2">
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Доступные языки</label>
                        <input
                            type="text"
                            value={value.allowedLanguages}
                            disabled={disabled}
                            onChange={(e) => patch({ allowedLanguages: e.target.value })}
                            className={`${inputClass} bg-white`}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};
