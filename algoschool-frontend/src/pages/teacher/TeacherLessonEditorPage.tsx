import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, FileText, CheckSquare, Type, Plus, Trash2 } from 'lucide-react';
import { apiClient } from '../../api/axios';

type StepType = 'THEORY' | 'CODE_PROBLEM' | 'INPUT_PROBLEM' | 'CHOICE_PROBLEM';

export const TeacherLessonEditorPage = () => {
    const { lessonId } = useParams<{ lessonId: string }>();
    const navigate = useNavigate();
    
    // Общие поля для всех шагов
    const [stepType, setStepType] = useState<StepType>('THEORY');
    const [orderIndex, setOrderIndex] = useState(1);
    const [isSaving, setIsSaving] = useState(false);
    
    // THEORY
    const [content, setContent] = useState('');
    
    // Общие поля для всех ЗАДАЧ (CODE, INPUT, CHOICE)
    const [description, setDescription] = useState('');
    const [difficulty, setDifficulty] = useState(1);
    const [xpReward, setXpReward] = useState(10);
    
    // Специфичные поля для CODE_PROBLEM
    const [timeLimitSec, setTimeLimitSec] = useState(2);
    const [memoryLimitMb, setMemoryLimitMb] = useState(256);
    const [allowedLanguages, setAllowedLanguages] = useState('Java, Python, C++');
    
    // Специфичные поля для INPUT_PROBLEM
    const [correctAnswer, setCorrectAnswer] = useState('');
    
    // Специфичные поля для CHOICE_PROBLEM
    const [options, setOptions] = useState<string[]>(['Первый вариант', 'Второй вариант']);
    const [correctOptionIndex, setCorrectOptionIndex] = useState<number>(0);

    // Управление вариантами ответов для тестов
    const handleAddOption = () => setOptions([...options, 'Новый вариант']);
    const handleRemoveOption = (index: number) => {
        if (options.length <= 2) return; // Оставляем минимум 2 варианта
        setOptions(options.filter((_, i) => i !== index));
        if (correctOptionIndex === index) setCorrectOptionIndex(0);
        else if (correctOptionIndex > index) setCorrectOptionIndex(prev => prev - 1);
    };
    const handleOptionChange = (text: string, index: number) => {
        const newOptions = [...options];
        newOptions[index] = text;
        setOptions(newOptions);
    };

    const handleSaveStep = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);

        // 1. Формируем базовый JSON
        let payload: any = { stepType, orderIndex };

        // 2. Подмешиваем специфичные поля
        if (stepType === 'THEORY') {
            payload.content = content;
        } else {
            // Если это любая задача, добавляем общие поля задачи
            payload.description = description;
            payload.difficulty = difficulty;
            payload.xpReward = xpReward;

            // И добавляем поля конкретного типа задачи
            if (stepType === 'CODE_PROBLEM') {
                payload.timeLimitSec = timeLimitSec;
                payload.memoryLimitMb = memoryLimitMb;
                payload.allowedLanguages = allowedLanguages;
            } else if (stepType === 'INPUT_PROBLEM') {
                payload.correctAnswer = correctAnswer;
            } else if (stepType === 'CHOICE_PROBLEM') {
                payload.options = options;
                payload.correctOptionIndex = correctOptionIndex;
            }
        }

        try {
            await apiClient.post(`/teacher/lessons/${lessonId}/steps`, payload);
            alert('Шаг успешно добавлен!');
            
            // Очищаем форму, увеличиваем индекс для следующего шага
            setOrderIndex(prev => prev + 1);
            setContent('');
            setDescription('');
            setCorrectAnswer('');
            // options не сбрасываем, чтобы было удобнее делать однотипные тесты
        } catch (error: any) {
            console.error(error);
            alert(error.response?.data?.message || 'Ошибка при сохранении шага. Проверьте консоль.');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto pb-12">
            <div className="flex items-center gap-4 mb-8 border-b border-slate-200 pb-6">
                <button 
                    onClick={() => navigate(-1)}
                    className="p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700 rounded-lg transition-colors"
                >
                    <ArrowLeft size={24} />
                </button>
                <div>
                    <h1 className="text-2xl font-bold text-slate-800">Редактор урока #{lessonId}</h1>
                    <p className="text-slate-500 text-sm">Добавление шагов (теория, тесты, код)</p>
                </div>
            </div>

            <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
                <div className="bg-slate-50 border-b border-slate-200 p-6">
                    <h2 className="text-lg font-bold text-slate-800">Новый шаг</h2>
                </div>
                
                <form onSubmit={handleSaveStep} className="p-6 space-y-6">
                    <div className="grid grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Тип шага</label>
                            <select 
                                value={stepType} 
                                onChange={(e) => setStepType(e.target.value as StepType)}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none bg-white font-medium"
                            >
                                <option value="THEORY">📚 Теория (Текст)</option>
                                <option value="CODE_PROBLEM">💻 Задача на код</option>
                                <option value="INPUT_PROBLEM">✍️ Ввод строки/числа</option>
                                <option value="CHOICE_PROBLEM">☑️ Тест с вариантами</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Порядковый номер</label>
                            <input 
                                type="number" min="1" required value={orderIndex} onChange={e => setOrderIndex(Number(e.target.value))}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                            />
                        </div>
                    </div>

                    {/* --- ТЕОРИЯ --- */}
                    {stepType === 'THEORY' && (
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-2">
                                <FileText size={16} className="text-blue-500"/> Текст теории (Markdown / HTML)
                            </label>
                            <textarea 
                                required value={content} onChange={e => setContent(e.target.value)}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none min-h-[200px]"
                                placeholder="Введите теоретический материал..."
                            />
                        </div>
                    )}

                    {/* --- ОБЩИЙ БЛОК ДЛЯ ВСЕХ ЗАДАЧ --- */}
                    {stepType !== 'THEORY' && (
                        <div className="space-y-4 border-t border-slate-100 pt-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Условие задачи / Текст вопроса</label>
                                <textarea 
                                    required value={description} onChange={e => setDescription(e.target.value)}
                                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none min-h-[120px]"
                                    placeholder="Напишите текст задания..."
                                />
                            </div>
                            
                            <div className="grid grid-cols-2 gap-6 bg-slate-50 p-4 rounded-lg border border-slate-200">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Сложность (1-10)</label>
                                    <input 
                                        type="number" min="1" max="10" required value={difficulty} onChange={e => setDifficulty(Number(e.target.value))}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Награда (XP)</label>
                                    <input 
                                        type="number" min="0" required value={xpReward} onChange={e => setXpReward(Number(e.target.value))}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                                    />
                                </div>
                            </div>

                            {/* --- СПЕЦИФИЧНЫЕ ПОЛЯ ДЛЯ CODE_PROBLEM --- */}
                            {stepType === 'CODE_PROBLEM' && (
                                <div className="grid grid-cols-2 gap-6 pt-2">
                                    <div>
                                        <label className="block text-sm font-medium text-slate-700 mb-1">Лимит времени (сек)</label>
                                        <input type="number" min="1" required value={timeLimitSec} onChange={e => setTimeLimitSec(Number(e.target.value))} className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-slate-700 mb-1">Лимит памяти (МБ)</label>
                                        <input type="number" min="16" required value={memoryLimitMb} onChange={e => setMemoryLimitMb(Number(e.target.value))} className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-slate-700 mb-1">Разрешенные языки</label>
                                        <input type="text" required value={allowedLanguages} onChange={e => setAllowedLanguages(e.target.value)} className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
                                    </div>
                                </div>
                            )}

                            {/* --- СПЕЦИФИЧНЫЕ ПОЛЯ ДЛЯ INPUT_PROBLEM --- */}
                            {stepType === 'INPUT_PROBLEM' && (
                                <div className="pt-2">
                                    <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-2">
                                        <Type size={16} className="text-emerald-600"/> Точный правильный ответ (строка или число)
                                    </label>
                                    <input 
                                        type="text" required value={correctAnswer} onChange={e => setCorrectAnswer(e.target.value)}
                                        className="w-full px-4 py-2 border-2 border-emerald-200 focus:border-emerald-500 rounded-lg outline-none text-emerald-900 bg-emerald-50"
                                        placeholder="Например: 42"
                                    />
                                    <p className="text-xs text-slate-500 mt-2">Студент должен будет ввести именно это значение.</p>
                                </div>
                            )}

                            {/* --- СПЕЦИФИЧНЫЕ ПОЛЯ ДЛЯ CHOICE_PROBLEM --- */}
                            {stepType === 'CHOICE_PROBLEM' && (
                                <div className="pt-2 bg-white border border-slate-200 rounded-xl p-4">
                                    <label className="block text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">
                                        <CheckSquare size={16} className="text-blue-500"/> Варианты ответов
                                    </label>
                                    
                                    <div className="space-y-3">
                                        {options.map((opt, index) => (
                                            <div key={index} className="flex items-center gap-3">
                                                {/* Радиокнопка для выбора правильного ответа */}
                                                <input 
                                                    type="radio" 
                                                    name="correctOption" 
                                                    checked={correctOptionIndex === index}
                                                    onChange={() => setCorrectOptionIndex(index)}
                                                    className="w-5 h-5 text-blue-600 focus:ring-blue-500 cursor-pointer"
                                                    title="Отметить как правильный"
                                                />
                                                <input 
                                                    type="text" required value={opt} onChange={e => handleOptionChange(e.target.value, index)}
                                                    className={`flex-1 px-4 py-2 border rounded-lg outline-none transition-colors ${correctOptionIndex === index ? 'border-emerald-500 bg-emerald-50 text-emerald-900' : 'border-slate-300 focus:border-blue-500'}`}
                                                    placeholder={`Вариант ${index + 1}`}
                                                />
                                                <button 
                                                    type="button" 
                                                    onClick={() => handleRemoveOption(index)} 
                                                    disabled={options.length <= 2}
                                                    className="p-2 text-slate-400 hover:text-red-500 disabled:opacity-50 transition-colors"
                                                >
                                                    <Trash2 size={20} />
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                    
                                    <button 
                                        type="button" 
                                        onClick={handleAddOption}
                                        className="mt-4 flex items-center gap-2 px-4 py-2 text-sm font-medium text-blue-600 hover:bg-blue-50 rounded-lg transition-colors border border-blue-200 border-dashed"
                                    >
                                        <Plus size={16} /> Добавить вариант
                                    </button>
                                </div>
                            )}

                        </div>
                    )}

                    <div className="pt-4 flex justify-end border-t border-slate-100 mt-8">
                        <button 
                            type="submit" disabled={isSaving}
                            className="flex items-center gap-2 px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-70"
                        >
                            {isSaving ? 'Сохранение...' : <><Save size={18} /> Сохранить шаг</>}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};