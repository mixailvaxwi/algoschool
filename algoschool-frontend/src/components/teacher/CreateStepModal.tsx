import React, { useState } from 'react';
import { X, Plus, Trash2 } from 'lucide-react';
import { apiClient } from '../../api/axios'; // Укажи правильный путь к твоему apiClient

interface CreateStepModalProps {
    isOpen: boolean;
    onClose: () => void;
    courseId: number;
    lessonId: number;
    onSuccess: () => void;
}

// Доступные типы шагов согласно бэкенду (StepServiceImpl.java)
type StepType = 'THEORY' | 'INPUT_PROBLEM' | 'CHOICE_PROBLEM' | 'CODE_PROBLEM';

export const CreateStepModal: React.FC<CreateStepModalProps> = ({ isOpen, onClose, courseId, lessonId, onSuccess }) => {
    // --- БАЗОВЫЕ ПОЛЯ ---
    const [stepType, setStepType] = useState<StepType>('THEORY');
    const [positionIndex, setPositionIndex] = useState<number>(1);

    // --- СПЕЦИФИЧНЫЕ ПОЛЯ ---
    const [content, setContent] = useState(''); // Для THEORY
    const [description, setDescription] = useState(''); // Общее для всех PROBLEM

    // Для INPUT_PROBLEM
    const [correctAnswer, setCorrectAnswer] = useState('');

    // Для CHOICE_PROBLEM
    const [options, setOptions] = useState<string[]>(['Вариант 1', 'Вариант 2']);
    const [correctOptionIndex, setCorrectOptionIndex] = useState<number>(0);

    // Для CODE_PROBLEM
    const [timeLimitSec, setTimeLimitSec] = useState<number>(2);
    const [memoryLimitMb, setMemoryLimitMb] = useState<number>(256);
    const [allowedLanguages, setAllowedLanguages] = useState('Java, Python, C++');
    const [ejudgeContestId, setEjudgeContestId] = useState<number | ''>('');
    const [ejudgeProblemId, setEjudgeProblemId] = useState('');

    const [isLoading, setIsLoading] = useState(false);

    if (!isOpen) return null;

    // Хендлеры для массива вариантов ответа (Тест)
    const handleOptionChange = (index: number, value: string) => {
        const newOptions = [...options];
        newOptions[index] = value;
        setOptions(newOptions);
    };

    const addOption = () => setOptions([...options, `Вариант ${options.length + 1}`]);

    const removeOption = (index: number) => {
        if (options.length <= 2) return; // Минимум 2 варианта
        const newOptions = options.filter((_, i) => i !== index);
        setOptions(newOptions);
        // Если удалили правильный вариант, сбрасываем на первый
        if (correctOptionIndex === index) setCorrectOptionIndex(0);
        else if (correctOptionIndex > index) setCorrectOptionIndex(correctOptionIndex - 1);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            // Формируем базовый payload (StepCreateRequest)
            const payload: any = {
                stepType,
                positionIndex: Number(positionIndex),
            };

            // Добавляем поля в зависимости от выбранного типа
            switch (stepType) {
                case 'THEORY':
                    payload.content = content;
                    break;
                case 'INPUT_PROBLEM':
                    payload.description = description;
                    payload.correctAnswer = correctAnswer;
                    break;
                case 'CHOICE_PROBLEM':
                    payload.description = description;
                    payload.options = options.filter(opt => opt.trim() !== '');
                    payload.correctOptionIndex = correctOptionIndex;
                    payload.isMultipleChoice = false; // В MVP делаем один правильный ответ
                    break;
                case 'CODE_PROBLEM':
                    payload.description = description;
                    payload.timeLimitSec = Number(timeLimitSec);
                    payload.memoryLimitMb = Number(memoryLimitMb);
                    payload.allowedLanguages = allowedLanguages;
                    payload.ejudgeContestId = Number(ejudgeContestId);
                    payload.ejudgeProblemId = ejudgeProblemId;
                    break;
            }

            // Отправляем запрос на эндпоинт учителя
            await apiClient.post(`/teacher/courses/${courseId}/lessons/${lessonId}/steps`, payload);

            onSuccess(); // Обновляем список шагов на странице
            onClose();   // Закрываем модалку
        } catch (error) {
            console.error("Ошибка при создании шага:", error);
            alert("Не удалось создать шаг. Проверьте заполнение полей.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl flex flex-col max-h-[90vh]">

                {/* Шапка */}
                <div className="flex justify-between items-center p-6 border-b">
                    <h2 className="text-xl font-bold text-slate-800">Добавить новый шаг</h2>
                    <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
                        <X size={24} />
                    </button>
                </div>

                {/* Скроллируемая форма */}
                <div className="p-6 overflow-y-auto">
                    <form id="step-form" onSubmit={handleSubmit} className="space-y-6">

                        {/* 1. БАЗОВЫЕ НАСТРОЙКИ (Видны всегда) */}
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Тип шага</label>
                                <select
                                    value={stepType}
                                    onChange={(e) => setStepType(e.target.value as StepType)}
                                    className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                >
                                    <option value="THEORY">📖 Теория</option>
                                    <option value="CHOICE_PROBLEM">✅ Тест (Один из многих)</option>
                                    <option value="INPUT_PROBLEM">⌨️ Точный ввод текста</option>
                                    <option value="CODE_PROBLEM">💻 Написание кода</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Порядковый номер</label>
                                <input
                                    type="number"
                                    min="1"
                                    value={positionIndex}
                                    onChange={(e) => setPositionIndex(Number(e.target.value))}
                                    className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                />
                            </div>
                        </div>

                        <hr className="border-slate-100" />

                        {/* 2. ДИНАМИЧЕСКИЕ ПОЛЯ */}

                        {/* --- ТЕОРИЯ --- */}
                        {stepType === 'THEORY' && (
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Текст теории (Поддерживает Markdown/HTML)</label>
                                <textarea
                                    required
                                    rows={8}
                                    value={content}
                                    onChange={(e) => setContent(e.target.value)}
                                    placeholder="Введите обучающий материал..."
                                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none resize-y"
                                />
                            </div>
                        )}

                        {/* --- ВСЕ ЗАДАЧИ: ОПИСАНИЕ УСЛОВИЯ --- */}
                        {stepType !== 'THEORY' && (
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Условие задачи</label>
                                <textarea
                                    required
                                    rows={4}
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    placeholder="Напишите, что нужно сделать студенту..."
                                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none resize-y"
                                />
                            </div>
                        )}

                        {/* --- ТОЧНЫЙ ВВОД (INPUT_PROBLEM) --- */}
                        {stepType === 'INPUT_PROBLEM' && (
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Правильный ответ (Строка)</label>
                                <input
                                    required
                                    type="text"
                                    value={correctAnswer}
                                    onChange={(e) => setCorrectAnswer(e.target.value)}
                                    placeholder="Например: 42"
                                    className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none font-mono"
                                />
                                <p className="text-xs text-slate-500 mt-1">Ответ студента будет проверяться на точное совпадение (без учета регистра и пробелов по краям).</p>
                            </div>
                        )}

                        {/* --- ТЕСТ (CHOICE_PROBLEM) --- */}
                        {stepType === 'CHOICE_PROBLEM' && (
                            <div className="space-y-3">
                                <label className="block text-sm font-medium text-slate-700">Варианты ответа (отметьте правильный)</label>
                                {options.map((opt, index) => (
                                    <div key={index} className="flex items-center gap-3">
                                        <input
                                            type="radio"
                                            name="correctOption"
                                            checked={correctOptionIndex === index}
                                            onChange={() => setCorrectOptionIndex(index)}
                                            className="w-5 h-5 text-blue-600 focus:ring-blue-500"
                                        />
                                        <input
                                            required
                                            type="text"
                                            value={opt}
                                            onChange={(e) => handleOptionChange(index, e.target.value)}
                                            className="flex-1 p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                            placeholder={`Вариант ${index + 1}`}
                                        />
                                        <button
                                            type="button"
                                            onClick={() => removeOption(index)}
                                            disabled={options.length <= 2}
                                            className="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                                        >
                                            <Trash2 size={20} />
                                        </button>
                                    </div>
                                ))}
                                <button
                                    type="button"
                                    onClick={addOption}
                                    className="flex items-center gap-2 text-sm font-medium text-blue-600 hover:text-blue-700 bg-blue-50 px-4 py-2 rounded-lg mt-2 transition-colors"
                                >
                                    <Plus size={16} /> Добавить вариант
                                </button>
                            </div>
                        )}

                        {/* --- НАПИСАНИЕ КОДА (CODE_PROBLEM) --- */}
                        {stepType === 'CODE_PROBLEM' && (
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Лимит времени (сек)</label>
                                    <input
                                        required
                                        type="number"
                                        min="1"
                                        value={timeLimitSec}
                                        onChange={(e) => setTimeLimitSec(Number(e.target.value))}
                                        className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Лимит памяти (МБ)</label>
                                    <input
                                        required
                                        type="number"
                                        min="16"
                                        value={memoryLimitMb}
                                        onChange={(e) => setMemoryLimitMb(Number(e.target.value))}
                                        className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                    />
                                </div>

                                <div className="col-span-2 grid grid-cols-2 gap-4 mt-2 p-4 bg-blue-50/50 rounded-xl border border-blue-100">
                                    <h4 className="col-span-2 text-sm font-bold text-blue-800 flex items-center gap-2">
                                        Интеграция с Ejudge
                                    </h4>
                                    <div>
                                        <label className="block text-xs font-semibold text-slate-700 mb-1">ID Турнира (Contest ID)</label>
                                        <input
                                            required
                                            type="number"
                                            value={ejudgeContestId}
                                            onChange={(e) => setEjudgeContestId(Number(e.target.value))}
                                            placeholder="Например: 101"
                                            className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-semibold text-slate-700 mb-1">ID Задачи (Short name)</label>
                                        <input
                                            required
                                            type="text"
                                            value={ejudgeProblemId}
                                            onChange={(e) => setEjudgeProblemId(e.target.value)}
                                            placeholder="Например: A или 1"
                                            className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                        />
                                    </div>
                                </div>

                                <div className="col-span-2">
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Доступные языки программирования</label>
                                    <input
                                        required
                                        type="text"
                                        value={allowedLanguages}
                                        onChange={(e) => setAllowedLanguages(e.target.value)}
                                        placeholder="Например: Java, Python, C++"
                                        className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                    />
                                </div>
                            </div>
                        )}

                    </form>
                </div>

                {/* Подвал с кнопками */}
                <div className="p-6 border-t bg-slate-50 flex justify-end gap-3 rounded-b-xl">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-5 py-2.5 text-slate-600 hover:bg-slate-200 bg-slate-100 rounded-lg font-medium transition-colors"
                    >
                        Отмена
                    </button>
                    <button
                        type="submit"
                        form="step-form"
                        disabled={isLoading}
                        className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors disabled:opacity-70 flex items-center gap-2"
                    >
                        {isLoading ? 'Сохранение...' : 'Создать шаг'}
                    </button>
                </div>

            </div>
        </div>
    );
};