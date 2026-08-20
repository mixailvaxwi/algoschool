import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/axios'; // Проверь путь до axios
import { PlusCircle, Save, ArrowLeft, Trash2, Hash, Plus, Pencil, X, BookOpen } from 'lucide-react';

type StepType = 'THEORY' | 'CHOICE_PROBLEM' | 'INPUT_PROBLEM' | 'CODE_PROBLEM';

interface StepDto {
    id: number;
    stepType: StepType;
    orderIndex: number;
    content?: string;
    description?: string;
    options?: string[];
    correctOptionIndexes?: number[];
    isMultipleChoice?: boolean;
    correctAnswer?: string;
    timeLimitSec?: number;
    memoryLimitMb?: number;
    allowedLanguages?: string;
    ejudgeContestId?: number;
    ejudgeProblemId?: string;
}

const STEP_TYPE_LABELS: Record<StepType, string> = {
    THEORY: '📖 Теория',
    CHOICE_PROBLEM: '🔘 Тест (с вариантами)',
    INPUT_PROBLEM: '⌨️ Точный ввод ответа',
    CODE_PROBLEM: '💻 Программирование'
};

export const LessonEditorPage = () => {
    const { courseId, lessonId } = useParams();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    // Список уже существующих шагов урока
    const [steps, setSteps] = useState<StepDto[]>([]);
    const [isLoadingSteps, setIsLoadingSteps] = useState(true);

    // null => создаём новый шаг, иначе — id редактируемого шага
    const [editingStepId, setEditingStepId] = useState<number | null>(null);

    // Состояния формы
    const [stepType, setStepType] = useState<StepType>('THEORY');
    const [orderIndex, setOrderIndex] = useState<number>(1);
    const [content, setContent] = useState('');
    const [description, setDescription] = useState('');

    // Специфичные состояния
    const [correctAnswer, setCorrectAnswer] = useState('');
    const [options, setOptions] = useState<string[]>(['Вариант 1', 'Вариант 2']);
    const [correctOptionIndexes, setCorrectOptionIndexes] = useState<number[]>([0]);
    const [isMultipleChoice, setIsMultipleChoice] = useState(false);
    const [timeLimit, setTimeLimit] = useState<number>(2);
    const [memoryLimit, setMemoryLimit] = useState<number>(256);
    const [allowedLanguages, setAllowedLanguages] = useState('Java, Python, C++');
    const [ejudgeContestId, setEjudgeContestId] = useState<number | ''>('');
    const [ejudgeProblemId, setEjudgeProblemId] = useState('');

    const fetchSteps = async () => {
        setIsLoadingSteps(true);
        try {
            const response = await apiClient.get<StepDto[]>(`/teacher/courses/${courseId}/lessons/${lessonId}/steps`);
            setSteps(response.data.sort((a, b) => a.orderIndex - b.orderIndex));
        } catch (error) {
            console.error('Ошибка загрузки шагов', error);
        } finally {
            setIsLoadingSteps(false);
        }
    };

    useEffect(() => {
        fetchSteps();
    }, [courseId, lessonId]);

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

        // Сдвигаем индексы правильных ответов вслед за удалённым вариантом;
        // если удалили единственный правильный — подставляем первый по умолчанию.
        const shifted = correctOptionIndexes
            .filter(i => i !== index)
            .map(i => (i > index ? i - 1 : i));
        setCorrectOptionIndexes(shifted.length > 0 ? shifted : [0]);
    };

    const toggleCorrectOption = (index: number) => {
        if (isMultipleChoice) {
            setCorrectOptionIndexes(prev =>
                prev.includes(index) ? prev.filter(i => i !== index) : [...prev, index]
            );
        } else {
            setCorrectOptionIndexes([index]);
        }
    };

    // Переключение обратно на одиночный выбор с несколькими отмеченными
    // вариантами оставило бы форму в состоянии, которое отклонит сервер.
    const handleMultipleChoiceToggle = (checked: boolean) => {
        setIsMultipleChoice(checked);
        if (!checked && correctOptionIndexes.length > 1) {
            setCorrectOptionIndexes([correctOptionIndexes[0]]);
        }
    };

    const resetForm = (nextOrderIndex: number) => {
        setEditingStepId(null);
        setStepType('THEORY');
        setOrderIndex(nextOrderIndex);
        setContent('');
        setDescription('');
        setCorrectAnswer('');
        setOptions(['Вариант 1', 'Вариант 2']);
        setCorrectOptionIndexes([0]);
        setIsMultipleChoice(false);
        setTimeLimit(2);
        setMemoryLimit(256);
        setAllowedLanguages('Java, Python, C++');
        setEjudgeContestId('');
        setEjudgeProblemId('');
    };

    const startEditingStep = (step: StepDto) => {
        setEditingStepId(step.id);
        setStepType(step.stepType);
        setOrderIndex(step.orderIndex);
        setContent(step.content ?? '');
        setDescription(step.description ?? '');
        setCorrectAnswer(step.correctAnswer ?? '');
        setOptions(step.options && step.options.length > 0 ? step.options : ['Вариант 1', 'Вариант 2']);
        setCorrectOptionIndexes(step.correctOptionIndexes && step.correctOptionIndexes.length > 0 ? step.correctOptionIndexes : [0]);
        setIsMultipleChoice(step.isMultipleChoice ?? false);
        setTimeLimit(step.timeLimitSec ?? 2);
        setMemoryLimit(step.memoryLimitMb ?? 256);
        setAllowedLanguages(step.allowedLanguages ?? 'Java, Python, C++');
        setEjudgeContestId(step.ejudgeContestId ?? '');
        setEjudgeProblemId(step.ejudgeProblemId ?? '');

        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const cancelEditing = () => resetForm(steps.length + 1);

    const handleDeleteStep = async (step: StepDto) => {
        if (!window.confirm(`Удалить шаг №${step.orderIndex}? Это действие необратимо.`)) return;

        try {
            await apiClient.delete(`/teacher/courses/${courseId}/lessons/${lessonId}/steps/${step.id}`);
            setSteps(steps.filter(s => s.id !== step.id));
            if (editingStepId === step.id) cancelEditing();
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось удалить шаг');
        }
    };

    const handleSaveStep = async () => {
        setIsLoading(true);
        const payload: any = { stepType, orderIndex };

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
                payload.correctOptionIndexes = correctOptionIndexes;
                payload.isMultipleChoice = isMultipleChoice;
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
            if (editingStepId) {
                const response = await apiClient.put<StepDto>(
                    `/teacher/courses/${courseId}/lessons/${lessonId}/steps/${editingStepId}`,
                    payload
                );
                setSteps(steps.map(s => s.id === editingStepId ? response.data : s).sort((a, b) => a.orderIndex - b.orderIndex));
                resetForm(steps.length + 1);
            } else {
                const response = await apiClient.post<StepDto>(
                    `/teacher/courses/${courseId}/lessons/${lessonId}/steps`,
                    payload
                );
                const nextSteps = [...steps, response.data].sort((a, b) => a.orderIndex - b.orderIndex);
                setSteps(nextSteps);

                // Авто-инкремент для следующего шага, остальное поле очищаем
                resetForm(orderIndex + 1);
            }
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

            {/* Список существующих шагов */}
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 mb-6">
                <h2 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                    <BookOpen className="text-blue-600" size={20} /> Шаги урока
                </h2>

                {isLoadingSteps ? (
                    <div className="text-slate-400 text-sm py-4">Загрузка шагов...</div>
                ) : steps.length === 0 ? (
                    <div className="text-slate-400 text-sm italic py-4">Шагов пока нет. Создайте первый ниже.</div>
                ) : (
                    <div className="space-y-2">
                        {steps.map(step => (
                            <div
                                key={step.id}
                                className={`flex items-center justify-between p-3 rounded-xl border transition-colors ${
                                    editingStepId === step.id
                                        ? 'border-blue-400 bg-blue-50'
                                        : 'border-slate-200 hover:bg-slate-50'
                                }`}
                            >
                                <div className="flex items-center gap-3 min-w-0">
                                    <span className="text-slate-400 font-mono text-sm w-6 text-right">{step.orderIndex}</span>
                                    <span className="text-sm font-medium text-slate-700 shrink-0">{STEP_TYPE_LABELS[step.stepType]}</span>
                                    <span className="text-sm text-slate-500 truncate">
                                        {step.stepType === 'THEORY' ? step.content : step.description}
                                    </span>
                                </div>
                                <div className="flex items-center gap-1 shrink-0 ml-3">
                                    <button
                                        onClick={() => startEditingStep(step)}
                                        className="p-2 text-slate-400 hover:text-amber-600 hover:bg-amber-100 rounded-lg transition-colors"
                                        title="Редактировать шаг"
                                    >
                                        <Pencil size={16} />
                                    </button>
                                    <button
                                        onClick={() => handleDeleteStep(step)}
                                        className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-100 rounded-lg transition-colors"
                                        title="Удалить шаг"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200">
                <div className="flex items-center justify-between mb-8">
                    <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                        <PlusCircle className="text-blue-600" /> {editingStepId ? `Редактирование шага №${orderIndex}` : 'Создание шага'}
                    </h1>
                    {editingStepId && (
                        <button
                            onClick={cancelEditing}
                            className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800 transition-colors"
                        >
                            <X size={16} /> Отменить редактирование
                        </button>
                    )}
                </div>

                {/* Базовые настройки */}
                <div className="grid grid-cols-2 gap-6 mb-8">
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Тип контента</label>
                        <select
                            value={stepType}
                            disabled={!!editingStepId}
                            onChange={(e) => setStepType(e.target.value as StepType)}
                            className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-60 disabled:cursor-not-allowed"
                        >
                            <option value="THEORY">📖 Теория</option>
                            <option value="CHOICE_PROBLEM">🔘 Тест (с вариантами)</option>
                            <option value="INPUT_PROBLEM">⌨️ Точный ввод ответа</option>
                            <option value="CODE_PROBLEM">💻 Программирование</option>
                        </select>
                        {editingStepId && (
                            <p className="text-xs text-slate-400 mt-1">Тип существующего шага изменить нельзя — удалите шаг и создайте новый.</p>
                        )}
                    </div>
                    <div>
                        <label className="block text-sm font-semibold text-slate-700 mb-2">Позиция в уроке (№)</label>
                        <div className="relative">
                            <Hash className="absolute left-3 top-3.5 text-slate-400" size={18} />
                            <input
                                type="number"
                                min="1"
                                value={orderIndex}
                                onChange={(e) => setOrderIndex(Number(e.target.value))}
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
                        <label className="flex items-center gap-3 mb-6 cursor-pointer w-fit">
                            <input
                                type="checkbox"
                                checked={isMultipleChoice}
                                onChange={(e) => handleMultipleChoiceToggle(e.target.checked)}
                                className="w-5 h-5 text-blue-600 focus:ring-blue-500 cursor-pointer rounded"
                            />
                            <span className="text-sm font-semibold text-slate-700">Несколько правильных ответов</span>
                        </label>
                        <label className="block text-sm font-semibold text-slate-700 mb-4">
                            {isMultipleChoice ? 'Варианты ответа (отметьте все правильные)' : 'Варианты ответа (отметьте правильный)'}
                        </label>
                        <div className="space-y-3">
                            {options.map((opt, index) => (
                                <div key={index} className="flex items-center gap-3 bg-white p-2 rounded-lg border border-slate-200">
                                    <input
                                        type={isMultipleChoice ? 'checkbox' : 'radio'}
                                        name="correctOption"
                                        checked={correctOptionIndexes.includes(index)}
                                        onChange={() => toggleCorrectOption(index)}
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
                        onClick={handleSaveStep}
                        disabled={isLoading}
                        className="bg-blue-600 text-white px-10 py-3 rounded-xl font-bold hover:bg-blue-700 transition-all disabled:opacity-50 flex items-center gap-2"
                    >
                        <Save size={20} /> {isLoading ? 'Сохранение...' : editingStepId ? 'Сохранить изменения' : 'Создать шаг'}
                    </button>
                </div>
            </div>
        </div>
    );
};
