import { Link, Outlet, useLocation } from 'react-router-dom';
import { BookOpen, Users, LayoutDashboard, ArrowLeft } from 'lucide-react';

export const AdminLayout = () => {
    const location = useLocation();

    const isActive = (path: string) => location.pathname.includes(path);

    return (
        <div className="flex min-h-screen bg-slate-100">
            {/* Боковое меню (Sidebar) */}
            <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col">
                <div className="p-6">
                    <h2 className="text-xl font-bold text-white flex items-center gap-2">
                        <LayoutDashboard className="text-blue-500" />
                        Algoschool
                    </h2>
                    <span className="text-xs text-slate-500 uppercase font-bold tracking-wider mt-1 block">
                        Кабинет преподавателя
                    </span>
                </div>

                <nav className="flex-1 px-4 space-y-2 mt-4">
                    <Link
                        to="/teacher/courses"
                        className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                            isActive('/teacher/courses') ? 'bg-blue-600 text-white' : 'hover:bg-slate-800 hover:text-white'
                        }`}
                    >
                        <BookOpen size={20} />
                        Управление курсами
                    </Link>

                    <Link
                        to="/teacher/users"
                        className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                            isActive('/teacher/users') ? 'bg-blue-600 text-white' : 'hover:bg-slate-800 hover:text-white'
                        }`}
                    >
                        <Users size={20} />
                        Пользователи
                    </Link>
                </nav>

                <div className="p-4 border-t border-slate-800">
                    <Link to="/" className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors text-sm">
                        <ArrowLeft size={16} />
                        Вернуться на сайт
                    </Link>
                </div>
            </aside>

            {/* Основная рабочая область */}
            <main className="flex-1 p-8 overflow-y-auto">
                <div className="max-w-6xl mx-auto bg-white rounded-xl shadow-sm border border-slate-200 min-h-[80vh] p-8">
                    {/* Сюда будут подставляться конкретные страницы админки */}
                    <Outlet />
                </div>
            </main>
        </div>
    );
};