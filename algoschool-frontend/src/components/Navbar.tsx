import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import {User, LogOut, Inbox, BookOpen, ShieldCheck} from 'lucide-react';

export const Navbar = () => {
    const navigate = useNavigate();
    // 2. Используем один стор как единственный источник правды
    const { user, isAuthenticated, logout } = useAuthStore();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isActive = (path: string) => {
        return location.pathname.startsWith(path);
    };

    const renderNavLinks = () => {
        if (!isAuthenticated) return null;

        const currentRole = user?.role?.toUpperCase() || '';

        if (currentRole.includes('ROLE_ADMIN')) {
            return (
                <nav className="hidden md:flex items-center gap-6 ml-10 font-medium text-slate-600">
                    <Link
                        to="/admin/users"
                        className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                            isActive('/admin') ? 'text-blue-600 bg-blue-50' : 'text-slate-600 hover:bg-slate-50'
                        }`}
                    >
                        <ShieldCheck size={18} />
                        <span className="font-medium">Панель администратора</span>
                    </Link>
                    <Link to="/courses" className="hover:text-blue-600 transition-colors">Каталог</Link>
                </nav>
            );
        }

        if (currentRole.includes('ROLE_TEACHER')) {
            return (
                <nav className="hidden md:flex items-center gap-6 ml-10 font-medium text-slate-600">
                    <Link to="/teacher/courses" className="hover:text-blue-600 transition-colors">Кабинет преподавателя</Link>
                    <Link
                        to="/courses/enrolled"
                        className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                            isActive('/courses/enrolled') ? 'text-blue-600 bg-blue-50' : 'text-slate-600 hover:bg-slate-50'
                        }`}
                    >
                        <BookOpen size={18} />
                        <span className="font-medium">Мое обучение</span>
                    </Link>
                    <Link to="/courses" className="hover:text-blue-600 transition-colors">Каталог</Link>
                </nav>
            );
        }

        return (
            <nav className="hidden md:flex items-center gap-6 ml-10 font-medium text-slate-600">
                <Link
                    to="/courses/enrolled"
                    className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                        isActive('/courses/enrolled') ? 'text-blue-600 bg-blue-50' : 'text-slate-600 hover:bg-slate-50'
                    }`}
                >
                    <BookOpen size={18} />
                    <span className="font-medium">Мое обучение</span>
                </Link>
                <Link to="/courses" className="hover:text-blue-600 transition-colors">Каталог</Link>
            </nav>
        );
    };

    return (
        <header className="flex justify-between items-center p-4 bg-white border-b shadow-sm h-16">
            <div className="flex items-center">
                <Link to="/" className="font-bold text-xl text-blue-600 cursor-pointer">
                    AlgoSchool
                </Link>
                {renderNavLinks()}
            </div>

            <div className="flex items-center gap-6">
                {isAuthenticated && user ? ( // 3. Проверяем и user, и isAuthenticated
                    <>
                        <div className="flex items-center gap-3 pl-5 border-l border-slate-200">
                            <div className="flex items-center gap-2 text-slate-700 font-medium">
                                <Link
                                    to="/my-applications"
                                    className="flex items-center gap-2 text-slate-600 hover:text-blue-600 transition-colors"
                                    title="Мои заявки"
                                >
                                    <Inbox size={20} />
                                    <span className="hidden md:block text-sm font-medium">Мои заявки</span>
                                </Link>

                                <Link
                                    to="/profile"
                                    title="Перейти в профиль"
                                    className="flex items-center gap-2 text-slate-700 font-medium hover:text-blue-600 transition-colors cursor-pointer"
                                >
                                    <div className="bg-slate-100 p-1.5 rounded-full">
                                        <User size={16} />
                                    </div>
                                    <span className="hidden sm:block">{user.username || 'Загрузка...'}</span>
                                </Link>
                            </div>

                            <button
                                onClick={handleLogout}
                                title="Выйти из аккаунта"
                                className="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"
                            >
                                <LogOut size={18} />
                            </button>
                        </div>
                    </>
                ) : (
                    <div className="flex gap-4 font-medium">
                        <Link to="/login" className="text-slate-600 hover:text-blue-600 px-4 py-2 transition-colors">Войти</Link>
                        <Link to="/register" className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors">Регистрация</Link>
                    </div>
                )}
            </div>
        </header>
    );
};