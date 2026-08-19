// @ts-ignore
import React, { useState, useEffect, useMemo } from 'react';
import { apiClient } from '../../api/axios';
// @ts-ignore
import { CheckCircle, XCircle, Clock, Terminal, Search, Filter, ArrowUpDown } from 'lucide-react';

interface TeacherSubmissionDto {
    id: number;
    studentId: number;
    studentName: string;
    courseId: number;
    courseTitle: string;
    moduleIndex: number;
    lessonIndex: number;
    stepIndex: number;
    payload: string;
    status: string;
    createdAt: string;
}

type SortKey = 'createdAt' | 'studentName' | 'courseTitle' | 'status';

export const TeacherSubmissionsPage = () => {
    const [submissions, setSubmissions] = useState<TeacherSubmissionDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // Состояния фильтров
    const [filterCourseId, setFilterCourseId] = useState<string>('');
    const [filterStudentId, setFilterStudentId] = useState<string>('');
    const [filterStatus, setFilterStatus] = useState<string>('');

    // Состояния сортировки (на клиенте)
    const [sortKey, setSortKey] = useState<SortKey>('createdAt');
    const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

    // Список своих курсов для выпадающего фильтра
    const [myCourses, setMyCourses] = useState<{ id: number; title: string }[]>([]);

    useEffect(() => {
        apiClient.get<{ id: number; title: string }[]>('/teacher/courses')
            .then((res) => {
                setMyCourses(res.data);
                // Журнал всегда показывается в разрезе одного курса — выбираем первый
                if (res.data.length > 0) {
                    setFilterCourseId(String(res.data[0].id));
                } else {
                    setIsLoading(false);
                }
            })
            .catch((error) => {
                console.error('Ошибка загрузки списка курсов', error);
                setIsLoading(false);
            });
    }, []);

    const fetchSubmissions = async () => {
        setIsLoading(true);
        try {
            const params: any = { courseId: filterCourseId };
            if (filterStudentId) params.studentId = filterStudentId;
            if (filterStatus) params.status = filterStatus;

            const response = await apiClient.get('/teacher/submissions', { params });
            setSubmissions(response.data);
        } catch (error) {
            console.error("Ошибка загрузки решений", error);
        } finally {
            setIsLoading(false);
        }
    };

    // Загружаем данные при изменении фильтров.
    // Без выбранного курса не запрашиваем: courseId обязателен на сервере.
    useEffect(() => {
        if (!filterCourseId) {
            setSubmissions([]);
            return;
        }
        // Дебаунс: чтобы не спамить бэкенд при каждом вводе символа
        const timer = setTimeout(() => {
            fetchSubmissions();
        }, 500);
        return () => clearTimeout(timer);
    }, [filterCourseId, filterStudentId, filterStatus]);

    // Локальная сортировка кликом по заголовкам таблицы
    const sortedSubmissions = useMemo(() => {
        return [...submissions].sort((a, b) => {
            let aValue = a[sortKey];
            let bValue = b[sortKey];

            if (aValue < bValue) return sortOrder === 'asc' ? -1 : 1;
            if (aValue > bValue) return sortOrder === 'asc' ? 1 : -1;
            return 0;
        });
    }, [submissions, sortKey, sortOrder]);

    const handleSort = (key: SortKey) => {
        if (sortKey === key) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            setSortKey(key);
            setSortOrder('asc');
        }
    };

    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'CORRECT': return <span className="flex items-center gap-1 text-emerald-600 bg-emerald-50 px-2 py-1 rounded-md text-xs font-bold"><CheckCircle size={14}/> Верно</span>;
            case 'WRONG_ANSWER': return <span className="flex items-center gap-1 text-red-600 bg-red-50 px-2 py-1 rounded-md text-xs font-bold"><XCircle size={14}/> Ошибка</span>;
            case 'PENDING': return <span className="flex items-center gap-1 text-amber-600 bg-amber-50 px-2 py-1 rounded-md text-xs font-bold"><Clock size={14}/> Проверяется</span>;
            case 'COMPILATION_ERROR': return <span className="flex items-center gap-1 text-purple-600 bg-purple-50 px-2 py-1 rounded-md text-xs font-bold"><Terminal size={14}/> Ош. компиляции</span>;
            default: return <span className="text-slate-500 text-xs">{status}</span>;
        }
    };

    return (
        <div className="max-w-7xl mx-auto">
            <h1 className="text-2xl font-bold text-slate-800 mb-6">Журнал решений студентов</h1>

            {/* ПАНЕЛЬ ФИЛЬТРОВ */}
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm mb-6 flex flex-wrap items-end gap-4">
                <div className="flex items-center gap-2 text-slate-500 mr-2">
                    <Filter size={20} />
                    <span className="font-medium text-sm">Фильтры:</span>
                </div>

                {/*
                  Курс выбирается из собственных курсов преподавателя: сервер
                  теперь требует courseId и проверяет авторство, поэтому поле
                  «введите любой ID» больше не имеет смысла.
                */}
                <div>
                    <label className="block text-xs font-medium text-slate-500 mb-1">Курс</label>
                    <select
                        value={filterCourseId}
                        onChange={(e) => setFilterCourseId(e.target.value)}
                        className="min-w-48 px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-500 outline-none bg-white"
                    >
                        {myCourses.length === 0 && <option value="">Нет курсов</option>}
                        {myCourses.map((c) => (
                            <option key={c.id} value={String(c.id)}>{c.title}</option>
                        ))}
                    </select>
                </div>

                <div>
                    <label className="block text-xs font-medium text-slate-500 mb-1">ID Студента</label>
                    <input
                        type="number"
                        placeholder="Все"
                        value={filterStudentId}
                        onChange={(e) => setFilterStudentId(e.target.value)}
                        className="w-24 px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-500 outline-none"
                    />
                </div>

                <div>
                    <label className="block text-xs font-medium text-slate-500 mb-1">Статус решения</label>
                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-500 outline-none bg-white"
                    >
                        <option value="">Все статусы</option>
                        <option value="CORRECT">✅ Верно</option>
                        <option value="WRONG_ANSWER">❌ Неверно</option>
                        <option value="PENDING">⏳ Проверяется</option>
                    </select>
                </div>
            </div>

            {/* ТАБЛИЦА */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse whitespace-nowrap">
                        <thead>
                        <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 text-sm">
                            <th className="py-3 px-4 font-semibold cursor-pointer hover:bg-slate-100 transition-colors" onClick={() => handleSort('createdAt')}>
                                <div className="flex items-center gap-1">Дата <ArrowUpDown size={14} className="opacity-50"/></div>
                            </th>
                            <th className="py-3 px-4 font-semibold cursor-pointer hover:bg-slate-100 transition-colors" onClick={() => handleSort('studentName')}>
                                <div className="flex items-center gap-1">Студент <ArrowUpDown size={14} className="opacity-50"/></div>
                            </th>
                            <th className="py-3 px-4 font-semibold cursor-pointer hover:bg-slate-100 transition-colors" onClick={() => handleSort('courseTitle')}>
                                <div className="flex items-center gap-1">Курс / Расположение <ArrowUpDown size={14} className="opacity-50"/></div>
                            </th>
                            <th className="py-3 px-4 font-semibold cursor-pointer hover:bg-slate-100 transition-colors" onClick={() => handleSort('status')}>
                                <div className="flex items-center gap-1">Статус <ArrowUpDown size={14} className="opacity-50"/></div>
                            </th>
                            <th className="py-3 px-4 font-semibold">Ответ (Код / Текст)</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 text-sm">
                        {isLoading ? (
                            <tr><td colSpan={5} className="py-12 text-center text-slate-400">Загрузка данных...</td></tr>
                        ) : sortedSubmissions.length === 0 ? (
                            <tr><td colSpan={5} className="py-12 text-center text-slate-400">По вашему запросу ничего не найдено</td></tr>
                        ) : (
                            sortedSubmissions.map((sub) => (
                                <tr key={sub.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="py-3 px-4 text-slate-500">
                                        {new Date(sub.createdAt).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}
                                    </td>
                                    <td className="py-3 px-4">
                                        <div className="font-medium text-slate-800">{sub.studentName}</div>
                                        <div className="text-xs text-slate-400">ID: {sub.studentId}</div>
                                    </td>
                                    <td className="py-3 px-4">
                                        <div className="font-medium text-slate-800">{sub.courseTitle}</div>
                                        <div className="text-xs text-slate-500">М{sub.moduleIndex} У{sub.lessonIndex} Шаг {sub.stepIndex}</div>
                                    </td>
                                    <td className="py-3 px-4">
                                        {getStatusIcon(sub.status)}
                                    </td>
                                    <td className="py-3 px-4 max-w-xs">
                                        <div className="bg-slate-100 px-2 py-1 rounded text-xs font-mono text-slate-600 truncate border border-slate-200" title={sub.payload}>
                                            {sub.payload}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};