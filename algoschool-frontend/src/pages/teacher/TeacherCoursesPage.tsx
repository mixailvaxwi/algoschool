import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, FolderEdit, Trash2, X, Save, Settings, Eye, EyeOff, Users } from 'lucide-react';
import { apiClient } from '../../api/axios';
import { Link } from 'react-router-dom';

// DTO Ответа (CourseResponse)
export interface CourseResponse {
    id: number;
    title: string;
    description: string;
    accessType: 'OPEN' | 'CLOSED';
    isPublished: boolean;
    authorName?: string;
}

// DTO Запроса (CourseRequest)
interface CourseRequest {
    title: string;
    description: string;
    accessType: 'OPEN' | 'CLOSED';
}

const DEFAULT_FORM: CourseRequest = {
    title: '',
    description: '',
    accessType: 'OPEN'
};

export const TeacherCoursesPage = () => {
    const navigate = useNavigate();
    
    const [courses, setCourses] = useState<CourseResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // Состояния модального окна
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editCourseId, setEditCourseId] = useState<number | null>(null);
    const [formData, setFormData] = useState<CourseRequest>(DEFAULT_FORM);

    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchCourses();
    }, []);

    const fetchCourses = async () => {
        try {
            const response = await apiClient.get<CourseResponse[]>('/teacher/courses');
            setCourses(response.data);
        } catch (error) {
            console.error('Ошибка загрузки курсов', error);
        } finally {
            setIsLoading(false);
        }
    };

    // Открыть модалку для СОЗДАНИЯ
    const handleOpenCreateModal = () => {
        setEditCourseId(null);
        setFormData(DEFAULT_FORM);
        setError('');
        setIsModalOpen(true);
    };

    // Открыть модалку для РЕДАКТИРОВАНИЯ
    const handleOpenEditModal = (course: CourseResponse) => {
        setEditCourseId(course.id);
        setFormData({
            title: course.title,
            description: course.description,
            accessType: course.accessType
        });
        setError('');
        setIsModalOpen(true);
    };

    // Функция отправки формы (Умная: сама понимает POST это или PUT)
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        setError('');

        try {
            if (editCourseId) {
                // ОБНОВЛЕНИЕ КУРСА
                const response = await apiClient.put<CourseResponse>(`/teacher/courses/${editCourseId}`, formData);
                setCourses(courses.map(c => c.id === editCourseId ? response.data : c));
            } else {
                // СОЗДАНИЕ КУРСА
                const response = await apiClient.post<CourseResponse>('/teacher/courses', formData);
                setCourses([response.data, ...courses]);
            }
            setIsModalOpen(false);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Произошла ошибка при сохранении');
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Вы уверены, что хотите удалить этот курс? Все модули, уроки и задачи также будут безвозвратно удалены!')) return;

        try {
            await apiClient.delete(`/teacher/courses/${id}`);
            setCourses(courses.filter(c => c.id !== id));
        } catch (error) {
            alert('Не удалось удалить курс. Возможно, есть связанные данные.');
        }
    };

    const handleTogglePublish = async (id: number) => {
        try {
            await apiClient.patch(`/teacher/courses/${id}/publish`);
            setCourses(courses.map(c =>
                c.id === id ? { ...c, isPublished: !c.isPublished } : c
            ));
        } catch (error) {
            alert('Не удалось изменить статус публикации');
        }
    };

    if (isLoading) return <div className="p-8 text-slate-500 text-center">Загрузка каталога...</div>;

    return (
        <div className="relative max-w-6xl mx-auto py-8">
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-slate-800">Управление курсами</h1>
                    <p className="text-slate-500 text-sm mt-1">Создавайте курсы и управляйте доступом студентов</p>
                </div>
                <button
                    onClick={handleOpenCreateModal}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl font-medium transition-colors shadow-sm"
                >
                    <Plus size={20} /> Создать курс
                </button>
            </div>

            <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-sm">
                        <th className="py-4 px-6 font-semibold w-16">ID</th>
                        <th className="py-4 px-6 font-semibold">Название курса</th>
                        <th className="py-4 px-6 font-semibold w-32">Тип доступа</th>
                        <th className="py-4 px-6 font-semibold w-32">Статус</th>
                        <th className="py-4 px-6 font-semibold text-right w-48">Действия</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                    {courses.length === 0 ? (
                        <tr>
                            <td colSpan={5} className="py-12 text-center text-slate-500">
                                У вас пока нет курсов. Нажмите «Создать курс», чтобы начать.
                            </td>
                        </tr>
                    ) : (
                        courses.map(course => (
                            <tr key={course.id} className="hover:bg-blue-50/50 transition-colors group">
                                <td className="py-4 px-6 text-slate-400 font-medium">#{course.id}</td>
                                <td className="py-4 px-6 font-bold text-slate-800">{course.title}</td>
                                <td className="py-4 px-6">
                                        <span className="text-slate-700 font-medium bg-slate-100 px-3 py-1 rounded-lg text-sm">
                                            {course.accessType === 'OPEN' ? '🔓 Открытый' : '🔒 По заявкам'}
                                        </span>
                                </td>
                                <td className="py-4 px-6">
                                        <span className={`px-3 py-1 rounded-lg text-xs font-bold ${
                                            course.isPublished ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                                        }`}>
                                            {course.isPublished ? 'Опубликован' : 'Черновик'}
                                        </span>
                                </td>
                                <td className="py-4 px-6 text-right">
                                    <div className="flex justify-end gap-1 opacity-60 group-hover:opacity-100 transition-opacity">
                                        <button
                                            onClick={() => navigate(`/teacher/courses/${course.id}/applications`)}
                                            className="p-2 text-slate-500 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                                            title="Заявки на курс"
                                        >
                                            <Users size={18} />
                                        </button>
                                        <button
                                            onClick={() => handleOpenEditModal(course)}
                                            className="p-2 text-slate-500 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
                                            title="Настройки курса (Название, доступ)"
                                        >
                                            <Settings size={18} />
                                        </button>
                                        <Link
                                            to={`/teacher/courses/${course.id}`}
                                            className="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                            title="Структура (Модули, Уроки, Задачи)"
                                        >
                                            <FolderEdit size={18} />
                                        </Link>
                                        <button
                                            onClick={() => handleDelete(course.id)}
                                            className="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                            title="Удалить курс"
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                        <button
                                            onClick={() => handleTogglePublish(course.id)}
                                            className={`p-2 rounded-lg transition-colors ${
                                                course.isPublished
                                                    ? 'text-emerald-500 hover:bg-emerald-50'
                                                    : 'text-slate-500 hover:text-blue-600 hover:bg-blue-50'
                                            }`}
                                            title={course.isPublished ? "Снять с публикации" : "Опубликовать курс"}
                                        >
                                            {course.isPublished ? <EyeOff size={18} /> : <Eye size={18} />}
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            {/* Модальное окно (Универсальное для Создания и Редактирования) */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg border border-slate-200 flex flex-col max-h-[90vh]">
                        <div className="flex justify-between items-center p-6 border-b border-slate-100">
                            <h2 className="text-xl font-bold text-slate-800">
                                {editCourseId ? 'Настройки курса' : 'Создание нового курса'}
                            </h2>
                            <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 transition-colors">
                                <X size={24} />
                            </button>
                        </div>

                        <div className="p-6 overflow-y-auto">
                            {error && <div className="mb-4 p-4 bg-red-50 text-red-600 rounded-xl text-sm font-medium border border-red-100">{error}</div>}

                            <form id="course-form" onSubmit={handleSubmit} className="space-y-5">
                                {/* Название курса */}
                                <div>
                                    <label className="block text-sm font-bold text-slate-700 mb-1.5">Название курса</label>
                                    <input
                                        type="text" required
                                        value={formData.title}
                                        onChange={e => setFormData({...formData, title: e.target.value})}
                                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all focus:bg-white"
                                        placeholder="Например: Введение в Python"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-bold text-slate-700 mb-1.5">Описание курса</label>
                                    <textarea
                                        required
                                        value={formData.description}
                                        onChange={e => setFormData({...formData, description: e.target.value})}
                                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none resize-none h-32 transition-all focus:bg-white"
                                        placeholder="О чем этот курс? Чему научатся студенты?"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-bold text-slate-700 mb-1.5">Тип доступа</label>
                                    <select
                                        value={formData.accessType}
                                        onChange={e => setFormData({...formData, accessType: e.target.value as 'OPEN' | 'CLOSED'})}
                                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all focus:bg-white cursor-pointer"
                                    >
                                        <option value="OPEN">🔓 Открытый (Любой может записаться)</option>
                                        <option value="CLOSED">🔒 Закрытый (Только по заявкам)</option>
                                    </select>
                                </div>
                            </form>
                        </div>

                        <div className="p-6 border-t border-slate-100 bg-slate-50 flex justify-end gap-3 rounded-b-2xl">
                            <button
                                type="button" onClick={() => setIsModalOpen(false)}
                                className="px-5 py-2.5 text-slate-600 hover:bg-slate-200 font-bold rounded-xl transition-colors"
                            >
                                Отмена
                            </button>
                            <button
                                type="submit" form="course-form" disabled={isSaving}
                                className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl transition-colors disabled:opacity-70 shadow-sm"
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