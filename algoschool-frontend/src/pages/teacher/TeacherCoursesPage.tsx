import { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, X, Save } from 'lucide-react';
import { apiClient } from '../../api/axios';
import { Link } from 'react-router-dom';

interface AdminCourse {
    id: number;
    title: string;
    description: string;
    price: number;
    isPublished: boolean;
}

export const AdminCoursesPage = () => {
    const [courses, setCourses] = useState<AdminCourse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // Состояния для модального окна создания курса
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newTitle, setNewTitle] = useState('');
    const [newDescription, setNewDescription] = useState('');
    const [newPrice, setNewPrice] = useState(0);
    const [newIsPublished, setNewIsPublished] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchCourses();
    }, []);

    const fetchCourses = async () => {
        try {
            const response = await apiClient.get('/admin/courses');
            setCourses(response.data);
        } catch (error) {
            console.error('Ошибка загрузки', error);
        } finally {
            setIsLoading(false);
        }
    };

    // Функция отправки формы
    const handleCreateCourse = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        setError('');

        try {
            const response = await apiClient.post('/admin/courses', {
                title: newTitle,
                description: newDescription,
                price: newPrice,
                isPublished: newIsPublished
            });

            // Добавляем новый курс в начало таблицы без перезагрузки страницы
            setCourses([response.data, ...courses]);

            // Закрываем и очищаем форму
            setIsModalOpen(false);
            setNewTitle('');
            setNewDescription('');
            setNewPrice(0);
            setNewIsPublished(false);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Ошибка при создании курса');
        } finally {
            setIsSaving(false);
        }
    };

    // Функция удаления
    const handleDelete = async (id: number) => {
        if (!window.confirm('Вы уверены, что хотите удалить этот курс? Это действие необратимо!')) return;

        try {
            await apiClient.delete(`/admin/courses/${id}`);
            setCourses(courses.filter(c => c.id !== id));
        } catch (error) {
            alert('Не удалось удалить курс');
        }
    };

    if (isLoading) return <div className="p-8 text-slate-500">Загрузка каталога...</div>;

    return (
        <div className="relative">
            {/* Заголовок и кнопка создания */}
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-2xl font-bold text-slate-800">Курсы</h1>
                    <p className="text-slate-500 text-sm mt-1">Управление каталогом и учебными материалами</p>
                </div>
                <button
                    onClick={() => setIsModalOpen(true)}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-colors"
                >
                    <Plus size={20} />
                    Создать курс
                </button>
            </div>

            {/* Таблица курсов */}
            <div className="overflow-x-auto bg-white border border-slate-200 rounded-lg shadow-sm">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-sm">
                        <th className="py-4 px-6 font-medium">ID</th>
                        <th className="py-4 px-6 font-medium">Название</th>
                        <th className="py-4 px-6 font-medium">Цена</th>
                        <th className="py-4 px-6 font-medium">Статус</th>
                        <th className="py-4 px-6 font-medium text-right">Действия</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200">
                    {courses.length === 0 ? (
                        <tr>
                            <td colSpan={5} className="py-8 text-center text-slate-500">Курсов пока нет. Создайте первый!</td>
                        </tr>
                    ) : (
                        courses.map(course => (
                            <tr key={course.id} className="hover:bg-slate-50 transition-colors">
                                <td className="py-4 px-6 text-slate-500">#{course.id}</td>
                                <td className="py-4 px-6 font-medium text-slate-800">{course.title}</td>
                                <td className="py-4 px-6 text-emerald-600 font-medium">{course.price > 0 ? `${course.price} XP` : 'Бесплатно'}</td>
                                <td className="py-4 px-6">
                                        <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                            course.isPublished ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'
                                        }`}>
                                            {course.isPublished ? 'Опубликован' : 'Черновик'}
                                        </span>
                                </td>
                                <td className="py-4 px-6 text-right">
                                    <div className="flex justify-end gap-2">
                                        {}
                                        <Link
                                            to={`/admin/courses/${course.id}`}
                                            className="p-2 text-slate-400 hover:text-blue-600 transition-colors"
                                            title="Редактировать контент"
                                        >
                                            <Edit size={18} />
                                        </Link>
                                        <button onClick={() => handleDelete(course.id)} className="p-2 text-slate-400 hover:text-red-600 transition-colors" title="Удалить курс">
                                            <Trash2 size={18} />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            {/* Модальное окно создания курса */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-slate-200 flex flex-col max-h-[90vh]">

                        <div className="flex justify-between items-center p-6 border-b border-slate-100">
                            <h2 className="text-xl font-bold text-slate-800">Новый курс</h2>
                            <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 transition-colors">
                                <X size={24} />
                            </button>
                        </div>

                        <div className="p-6 overflow-y-auto">
                            {error && <div className="mb-4 p-3 bg-red-50 text-red-600 rounded-lg text-sm border border-red-100">{error}</div>}

                            <form id="create-course-form" onSubmit={handleCreateCourse} className="space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Название курса</label>
                                    <input
                                        type="text" required value={newTitle} onChange={e => setNewTitle(e.target.value)}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                        placeholder="Например: Основы алгоритмов"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Описание</label>
                                    <textarea
                                        required value={newDescription} onChange={e => setNewDescription(e.target.value)}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none resize-none h-24"
                                        placeholder="Кратко о чем этот курс..."
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Стоимость (во внутренней валюте)</label>
                                    <input
                                        type="number" min="0" required value={newPrice} onChange={e => setNewPrice(Number(e.target.value))}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                                    />
                                    <p className="text-xs text-slate-500 mt-1">Оставьте 0, чтобы курс был бесплатным</p>
                                </div>

                                <label className="flex items-center gap-3 p-3 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 transition-colors">
                                    <input
                                        type="checkbox" checked={newIsPublished} onChange={e => setNewIsPublished(e.target.checked)}
                                        className="w-5 h-5 text-blue-600 rounded focus:ring-blue-500"
                                    />
                                    <div>
                                        <div className="font-medium text-slate-800">Опубликовать сразу</div>
                                        <div className="text-xs text-slate-500">Студенты увидят курс в каталоге</div>
                                    </div>
                                </label>
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
                                type="submit" form="create-course-form" disabled={isSaving}
                                className="flex items-center gap-2 px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-70"
                            >
                                {isSaving ? 'Сохранение...' : <><Save size={18} /> Создать</>}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};