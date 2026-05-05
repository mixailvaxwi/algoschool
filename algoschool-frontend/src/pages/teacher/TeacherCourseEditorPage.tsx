import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Folder, FileText, X, Save } from 'lucide-react';
import { apiClient } from '../../api/axios';

// Типы, соответствующие нашему CourseStructureResponse с бэкенда
interface Lesson {
    id: number;
    title: string;
    orderIndex: number;
}

interface Module {
    id: number;
    title: string;
    orderIndex: number;
    lessons: Lesson[];
}

export const AdminCourseEditorPage = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();

    const [modules, setModules] = useState<Module[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // Состояния для универсального модального окна
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalType, setModalType] = useState<'MODULE' | 'LESSON'>('MODULE');
    const [activeModuleId, setActiveModuleId] = useState<number | null>(null);

    // Данные формы
    const [newItemTitle, setNewItemTitle] = useState('');
    const [newItemOrder, setNewItemOrder] = useState(1);
    const [isSaving, setIsSaving] = useState(false);

    // Функция загрузки реальной структуры с бэкенда
    const fetchStructure = async () => {
        try {
            const response = await apiClient.get(`/admin/courses/${courseId}/structure`);

            // Сортируем модули и уроки по их orderIndex для правильного отображения
            const sortedModules = response.data.map((m: Module) => ({
                ...m,
                lessons: m.lessons.sort((a, b) => a.orderIndex - b.orderIndex)
            })).sort((a: Module, b: Module) => a.orderIndex - b.orderIndex);

            setModules(sortedModules);
        } catch (error) {
            console.error('Ошибка загрузки структуры', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchStructure();
    }, [courseId]);

    // Открытие модалки для модуля
    const openModuleModal = () => {
        setModalType('MODULE');
        setNewItemTitle('');
        setNewItemOrder(modules.length + 1); // Автоматически предлагаем следующий номер
        setIsModalOpen(true);
    };

    // Открытие модалки для урока
    const openLessonModal = (moduleId: number, currentLessonsCount: number) => {
        setModalType('LESSON');
        setActiveModuleId(moduleId);
        setNewItemTitle('');
        setNewItemOrder(currentLessonsCount + 1);
        setIsModalOpen(true);
    };

    // Обработка сохранения формы
    const handleSaveItem = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);

        try {
            if (modalType === 'MODULE') {
                await apiClient.post(`/admin/courses/${courseId}/modules`, {
                    title: newItemTitle,
                    orderIndex: newItemOrder
                });
            } else if (modalType === 'LESSON' && activeModuleId) {
                await apiClient.post(`/admin/modules/${activeModuleId}/lessons`, {
                    title: newItemTitle,
                    orderIndex: newItemOrder
                });
            }

            setIsModalOpen(false);
            fetchStructure(); // Обновляем дерево после сохранения
        } catch (error) {
            alert('Ошибка при сохранении');
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) return <div className="p-8 text-slate-500">Загрузка структуры курса...</div>;

    return (
        <div className="max-w-5xl mx-auto relative">
            <div className="flex items-center gap-4 mb-8 border-b border-slate-200 pb-6">
                <button
                    onClick={() => navigate('/admin/courses')}
                    className="p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700 rounded-lg transition-colors"
                    title="Назад к списку курсов"
                >
                    <ArrowLeft size={24} />
                </button>
                <div>
                    <h1 className="text-2xl font-bold text-slate-800">Редактор курса #{courseId}</h1>
                    <p className="text-slate-500 text-sm">Управление модулями и уроками</p>
                </div>
                <button
                    onClick={openModuleModal}
                    className="ml-auto flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-colors"
                >
                    <Plus size={20} />
                    Добавить модуль
                </button>
            </div>

            <div className="space-y-4">
                {modules.length === 0 ? (
                    <div className="text-center py-12 bg-slate-50 border border-slate-200 border-dashed rounded-xl">
                        <Folder className="mx-auto text-slate-300 mb-3" size={48} />
                        <h3 className="text-lg font-medium text-slate-700">В этом курсе пока нет модулей</h3>
                        <p className="text-slate-500 text-sm mt-1">Добавьте первый модуль, чтобы начать создавать уроки</p>
                    </div>
                ) : (
                    modules.map(module => (
                        <div key={module.id} className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
                            <div className="bg-slate-50 px-6 py-4 border-b border-slate-200 flex justify-between items-center">
                                <div className="flex items-center gap-3 font-medium text-slate-800">
                                    <Folder className="text-blue-500" size={20} />
                                    {module.orderIndex}. {module.title}
                                </div>
                                <button
                                    onClick={() => openLessonModal(module.id, module.lessons.length)}
                                    className="text-sm flex items-center gap-1 text-blue-600 hover:text-blue-700 font-medium px-3 py-1.5 hover:bg-blue-50 rounded-md transition-colors"
                                >
                                    <Plus size={16} /> Урок
                                </button>
                            </div>

                            <div className="p-4 bg-white">
                                {module.lessons.length === 0 ? (
                                    <div className="text-sm text-slate-400 pl-8 italic py-2">Уроков нет. Нажмите «+ Урок» справа.</div>
                                ) : (
                                    <div className="space-y-2">
                                        {module.lessons.map(lesson => (
                                            <div key={lesson.id} className="flex items-center justify-between pl-8 py-2 text-slate-700 hover:bg-slate-50 rounded-lg group">
                                                <div className="flex items-center gap-3">
                                                    <FileText className="text-slate-400 group-hover:text-blue-500 transition-colors" size={18} />
                                                    <span>{module.orderIndex}.{lesson.orderIndex}. {lesson.title}</span>
                                                </div>
                                                <button
                                                    className="opacity-0 group-hover:opacity-100 text-xs bg-white border border-slate-200 px-3 py-1 rounded text-slate-600 hover:text-blue-600 transition-all shadow-sm"
                                                    onClick={() => navigate(`/admin/lessons/${lesson.id}`)}
                                                >
                                                    Редактировать шаги
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>

            {/* Универсальное модальное окно */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-xl w-full max-w-md border border-slate-200 flex flex-col">
                        <div className="flex justify-between items-center p-6 border-b border-slate-100">
                            <h2 className="text-xl font-bold text-slate-800">
                                {modalType === 'MODULE' ? 'Новый модуль' : 'Новый урок'}
                            </h2>
                            <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 transition-colors">
                                <X size={24} />
                            </button>
                        </div>

                        <div className="p-6">
                            <form id="create-item-form" onSubmit={handleSaveItem} className="space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Название</label>
                                    <input
                                        type="text" required value={newItemTitle} onChange={e => setNewItemTitle(e.target.value)}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                        placeholder={modalType === 'MODULE' ? "Например: Введение в алгоритмы" : "Например: Что такое О-большое?"}
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Порядковый номер</label>
                                    <input
                                        type="number" min="1" required value={newItemOrder} onChange={e => setNewItemOrder(Number(e.target.value))}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                    />
                                </div>
                            </form>
                        </div>

                        <div className="p-6 border-t border-slate-100 bg-slate-50 flex justify-end gap-3 rounded-b-xl">
                            <button
                                type="button" onClick={() => setIsModalOpen(false)}
                                className="px-4 py-2 text-slate-600 hover:bg-slate-200 font-medium rounded-lg transition-colors"
                            >
                                Отмена
                            </button>
                            <button
                                type="submit" form="create-item-form" disabled={isSaving}
                                className="flex items-center gap-2 px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-70"
                            >
                                {isSaving ? 'Сохранение...' : <><Save size={18} /> Сохранить</>}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};