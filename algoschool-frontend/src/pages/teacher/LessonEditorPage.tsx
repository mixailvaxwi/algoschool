import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/axios'; // Проверь путь до axios
import { PlusCircle, Save, ArrowLeft, Trash2, Hash, Plus } from 'lucide-react';

type StepType = 'THEORY' | 'CHOICE_PROBLEM' | 'INPUT_PROBLEM' | 'CODE_PROBLEM';

export const LessonEditorPage = () => {
    const { courseId, lessonId } = useParams();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    // Состояния формы
    const [stepType, setStepType] = useState<StepType>('THEORY');
    const [positionIndex, setPositionIndex] = useState<number>(1);
    const [content, setContent] = useState('');
    const [description, setDescription] = useState('');

    // Специфичные состояния
    const [correctAnswer, setCorrectAnswer] = useState('');
    const [options, setOptions] = useState<string[]>(['Вариант 1', 'Вариант 2']);
    const [correctOptionIndex, setCorrectOptionIndex] = useState<number>(0);
    const [timeLimit, setTimeLimit] = useState<number>(2);
    const [memoryLimit, setMemoryLimit] = useState<number>(256);
    const [allowedLanguages, setAllowedLanguages] = useState('Java, Python, C++');
    const [ejudgeContestId, setEjudgeContestId] = useState<number | ''>('');
    const [ejudgeProblemId, setEjudgeProblemId] = useState('');

    // Функции для управления вариантами ответа (для тестов)
    const handleOptionChange = (index: number, value: string) => {
        const newOptions = [...options];
        newOptions[index] = value;
        setOptions(newOptions);
    };

    const addOption = () => setOptions([...options, `Вариант ${options.length + 1}`]);

    const removeOption = (index: number) => {
        if (options.length <= 2) return; // Меньше 2 вариантов оставлять нельзя
        const newOptions = options.filter((_, i) => i !== index);
        setOptions(newOptions);
        if (correctOptionIndex === index) setCorrectOptionIndex(0);
        else if (correctOptionIndex > index) setCorrectOptionIndex(correctOptionIndex - 1);
    };

    const handleCreateStep = async () => {
        setIsLoading(true);
        const payload: any = { stepType, positionIndex };

        // Собираем данные в зависимости от типа
        if (stepType === 'THEORY') {
            payload.content = content;
        } else {
            payload.description = description;
            if (stepType === 'INPUT_PROBLEM') {
                payload.correctAnswer = correctAnswer;
            }
            if (stepType === 'CHOICE_PROBLEM') {
                payload.options = options.filter(opt => opt.trim() !== ''); // Убираем пустые
                payload.correctOptionIndex = correctOptionIndex;
                payload.isMultipleChoice = false; // По умолчанию 1 правильный ответ
            }
            if (stepType === 'CODE_PROBLEM') {
                payload.timeLimitSec = timeLimit;
                payload.memoryLimitMb = memoryLimit;
                payload.allowedLanguages = allowedLanguages;
                payload.ejudgeContestId = Number(ejudgeContestId);
                payload.ejudgeProblemId = ejudgeProblemId;
            }
        }

        try {
            await apiClient.post(`/teacher/courses/${courseId}/lessons/${lessonId}/steps`, payload);
            alert('Шаг успешно создан!');

            // Авто-инкремент для следующего шага
            setPositionIndex(prev => prev + 1);

            // Очищаем текстовые поля для нового шага
            setContent('');
            setDescription('');
            setCorrectAnswer('');
            setEjudgeContestId('');
            setEjudgeProblemId('');
        } catch (error: any) {
            alert(error.response?.data?.message || 'Ошибка при сохранении');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="max-w-3xl mx-auto py-8">
            <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-slate-500 mb-6 hover:text-slate-800 transition-colors">
                <ArrowLeft size={18} /> К структуре курса
            </button>

            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200">
                <h1 className="text-2xl font-bold text-slate-800 mb-8 flex items-center gap-2">
                    <PlusCircle className="text-blue-600" /> Создание шага
                </h1>

                {/* Базовые настройки */}
                <div className="grid grid-cols-2 gap-6 mb-8">
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Тип контента</label>
                        <select
                            value={stepType}
                            onChange={(e) => setStepType(e.target.value as StepType)}
                            className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="THEORY">📖 Теория</option>
                            <option value="CHOICE_PROBLEM">🔘 Тест (с вариантами)</option>
                            <option value="INPUT_PROBLEM">⌨️ Точный ввод ответа</option>
                            <option value="CODE_PROBLEM">💻 Программирование</option>
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Позиция в уроке (№)</label>
                        <div className="relative">
                            <Hash className="absolute left-3 top-3.5 text-slate-400" size={18} />
                            <input
                                type="number"
                                min="1"
                                value={positionIndex}
                                onChange={(e) => setPositionIndex(Number(e.target.value))}
                                className="w-full p-3 pl-10 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>
                </div>

                {/* Поле текста (Content/Description) */}
                <div className="mb-6">
                    <label className="block text-sm font-semibold text-slate-700 mb-2">
                        {stepType === 'THEORY' ? 'Текст теории (HTML/Markdown)' : 'Условие задачи'}
                    </label>
                    <textarea
                        value={stepType === 'THEORY' ? content : description}
                        onChange={(e) => stepType === 'THEORY' ? setContent(e.target.value) : setDescription(e.target.value)}
                        className="w-full h-48 p-4 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 resize-y"
                        placeholder={stepType === 'THEORY' ? "Введите обучающий материал..." : "Напишите, что нужно сделать студенту..."}
                    />
                </div>

                {/* ДИНАМИЧЕСКИЕ БЛОКИ */}

                {/* 1. Блок для INPUT_PROBLEM */}
                {stepType === 'INPUT_PROBLEM' && (
                    <div className="mb-6 p-6 bg-slate-50 rounded-xl border border-slate-200">
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Правильный ответ (Строка)</label>
                        <input
                            type="text"
                            value={correctAnswer}
                            onChange={(e) => setCorrectAnswer(e.target.value)}
                            placeholder="Например: 42"
                            className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 font-mono"
                        />
                        <p className="text-sm text-slate-500 mt-2">Ответ проверяется на точное совпадение (без учета регистра).</p>
                    </div>
                )}

                {/* 2. Блок для CHOICE_PROBLEM */}
                {stepType === 'CHOICE_PROBLEM' && (
                    <div className="mb-6 p-6 bg-slate-50 rounded-xl border border-slate-200">
                        <label className="block text-sm font-semibold text-slate-700 mb-4">Варианты ответа (отметьте правильный)</label>
                        <div className="space-y-3">
                            {options.map((opt, index) => (
                                <div key={index} className="flex items-center gap-3 bg-white p-2 rounded-lg border border-slate-200">
                                    <input
                                        type="radio"
                                        name="correctOption"
                                        checked={correctOptionIndex === index}
                                        onChange={() => setCorrectOptionIndex(index)}
                                        className="w-5 h-5 ml-2 text-blue-600 focus:ring-blue-500 cursor-pointer"
                                    />
                                    <input
                                        type="text"
                                        value={opt}
                                        onChange={(e) => handleOptionChange(index, e.target.value)}
                                        className="flex-1 p-2 border-none outline-none focus:ring-0"
                                        placeholder={`Вариант ${index + 1}`}
                                    />
                                    <button
                                        onClick={() => removeOption(index)}
                                        disabled={options.length <= 2}
                                        className="p-2 text-slate-400 hover:text-red-500 disabled:opacity-30 transition-colors"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            ))}
                        </div>
                        <button
                            onClick={addOption}
                            className="mt-4 flex items-center gap-2 text-sm font-semibold text-blue-600 hover:text-blue-700 transition-colors"
                        >
                            <Plus size={16} /> Добавить вариант
                        </button>
                    </div>
                )}

                {/* 3. Блок для CODE_PROBLEM */}
                {stepType === 'CODE_PROBLEM' && (
                    <div className="mb-6 p-6 bg-slate-50 rounded-xl border border-slate-200 grid grid-cols-2 gap-6">

                        {/* --- ИНТЕГРАЦИЯ С EJUDGE --- */}
                        <div className="col-span-2 grid grid-cols-2 gap-6 p-5 bg-blue-50/50 rounded-xl border border-blue-100">
                            <h4 className="col-span-2 text-sm font-bold text-blue-800 flex items-center gap-2">
                                Интеграция с Ejudge
                            </h4>
                            <div>
                                <label className="block text-sm font-semibold text-slate-700 mb-2">ID Турнира (Contest ID)</label>
                                <input
                                    required
                                    type="number"
                                    value={ejudgeContestId}
                                    onChange={(e) => setEjudgeContestId(Number(e.target.value))}
                                    placeholder="Например: 101"
                                    className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-semibold text-slate-700 mb-2">ID Задачи (Short name)</label>
                                <input
                                    required
                                    type="text"
                                    value={ejudgeProblemId}
                                    onChange={(e) => setEjudgeProblemId(e.target.value)}
                                    placeholder="Например: 1 или A"
                                    className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                                />
                            </div>
                        </div>

                        {/* --- СТАНДАРТНЫЕ ПОЛЯ --- */}
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">Лимит времени (сек)</label>
                            <input
                                type="number"
                                min="1"
                                value={timeLimit}
                                onChange={(e) => setTimeLimit(Number(e.target.value))}
                                className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">Лимит памяти (МБ)</label>
                            <input
                                type="number"
                                min="16"
                                value={memoryLimit}
                                onChange={(e) => setMemoryLimit(Number(e.target.value))}
                                className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="col-span-2">
                            <label className="block text-sm font-semibold text-slate-700 mb-2">Доступные языки</label>
                            <input
                                type="text"
                                value={allowedLanguages}
                                onChange={(e) => setAllowedLanguages(e.target.value)}
                                className="w-full p-3 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>
                )}

                {/* Кнопка сохранения */}
                <div className="mt-8 pt-6 border-t border-slate-100 flex justify-end">
                    <button
                        onClick={handleCreateStep}
                        disabled={isLoading}
                        className="bg-blue-600 text-white px-10 py-3 rounded-xl font-bold hover:bg-blue-700 transition-all disabled:opacity-50 flex items-center gap-2"
                    >
                        <Save size={20} /> {isLoading ? 'Сохранение...' : 'Создать шаг'}
                    </button>
                </div>
            </div>
        </div>
    );
};